package caja1694.students.ju.se.artech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalTime;




@TargetApi(Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    final static double BREAK_ROOM = 0.1;
    final static double CONTROL_ROOM = 0.5;
    final static double REACTOR_ROOM = 1.6;

    final static Warning systemWideWarning = new Warning().SystemWideWarning();

    Warning warning = new Warning();

    final static String NuclearTechnician = "NuclearTechnician1";
    final static String OverstayedStatus = "OverstayedStatus";

    String currentTime = LocalTime.now().toString();
    String currentDate = LocalDate.now().toString();

    DatabaseManager dbManager = new DatabaseManager(NuclearTechnician, currentDate);

    Boolean isClockedIn = false;
    CountDownTimer mCountDownTimer;

    Button connectButton;
    Button clockInButton;

    BluetoothAdapter btAdapter;

    private Context mContext = this;
    TimeKeeper timeKeeper = new TimeKeeper(3000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        valueEventListener();
        verifyBluetooth();
        connectButton = findViewById(R.id.connect_button);
        clockFunction();

    }

    private void clockFunction() {
        clockInButton = findViewById(R.id.clock_in_button);
        clockInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClockedIn) { clockOut(); }
                else { clockIn(); }
            }
        });
    }

    void startTimer(){
        mCountDownTimer = new CountDownTimer(timeKeeper.getStartTimeInMillis(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeKeeper.setTimeLeftInMillis(millisUntilFinished);
                updateTimer(timeKeeper.toString());

                if(timeKeeper.timeLeftInSeconds() == 600){ warning.createMinuteWarning("10").sendthisWarning(mContext); }

                if(timeKeeper.timeLeftInSeconds() == 300){ warning.createMinuteWarning("5").sendthisWarning(mContext);  }

                if(timeKeeper.timeLeftInSeconds() == 60) { warning.createMinuteWarning("1").sendthisWarning(mContext);  }
            }
            @Override
            public void onFinish(){
                if(isClockedIn){
                    dbManager.setStatus(OverstayedStatus);
                }
            }
        }.start();
    }

    void stopTimer(){
        mCountDownTimer.cancel();
        timeKeeper.setTimeLeftInMillis(timeUntilRadiationLimit());
    }

    long timeUntilRadiationLimit(){
        Radiation radiationLevels = new Radiation(30, REACTOR_ROOM, 1); // This should be in TimeKeeper
        double exposurePerMiliSecond = radiationLevels.getUnitExposurePerMilliSecond();                       // taking the radiation-obj as
        double timeLeft = radiationLevels.getRadioationLimit()/exposurePerMiliSecond;                         // param and use
        return (long) timeLeft;                                                                               // timeKeeper.timeLEFTInMillis
    }                                                                                                         // to recalculate.

    void updateTimer(String timeLeft){
        TextView countdownText = findViewById(R.id.countdownTimer);
        countdownText.setText(timeLeft);
    }
    void clockIn(){
        isClockedIn = true;
        updateStatusText("Clocked In");
        dbManager.addClockInTime(currentTime);
        startTimer();
    }
    void clockOut(){
        isClockedIn = false;
        updateStatusText("Clocked Out");
        dbManager.addClockOutTime(currentTime);
        stopTimer();
    }
    void updateStatusText(String text){
        TextView textView = findViewById(R.id.status);
        textView.setText(text);
    }

    void valueEventListener(){
        ValueEventListener statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String technichianStatus = dataSnapshot.getValue().toString();
                    if (technichianStatus.equals(OverstayedStatus)) {
                        systemWideWarning.sendthisWarning(mContext);
                    }
                }
                catch (NullPointerException exception){
                    System.out.println("Nullpointer exception in valueEventListener, probably couz there are no clock ins yet on this date");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database error: "+databaseError, Toast.LENGTH_SHORT).show();
            }
        };
        dbManager.getStatusRef().addValueEventListener(statusListener);
    }

    private void verifyBluetooth(){
        int REQUEST_ENABLE_BT = 1;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null){
            Toast.makeText(MainActivity.this, "Device does not support bluetooth", Toast.LENGTH_SHORT).show();
        }
        else if(!btAdapter.isEnabled()){
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Toast.makeText(getApplicationContext(), "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
        }
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth cancelled, enable to connect to safety console", Toast.LENGTH_LONG).show();
        }
    }
}
