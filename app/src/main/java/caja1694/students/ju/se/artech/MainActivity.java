package caja1694.students.ju.se.artech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    DatabaseReference myRef =  FirebaseDatabase.getInstance().getReference();
    DatabaseReference statusReference = myRef.child(currentDate).child(NuclearTechnician).child("Status");

    Boolean isClockedIn = false;
    CountDownTimer mCountDownTimer;

    private NotificationManagerCompat notificationManager;

    TimeKeeper timeKeeper = new TimeKeeper(3000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        valueEventListener();

        final Button clockInButton = findViewById(R.id.clock_in_button);

        clockInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClockedIn) {
                    clockOut();
                }
                else {
                    clockIn();
                }
            }
        });
    }

    void startTimer(){
        mCountDownTimer = new CountDownTimer(timeKeeper.getStartTimeInMillis(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeKeeper.setTimeLeftInMillis(millisUntilFinished);
                updateTimer(timeKeeper.toString());

                if(timeKeeper.timeLeftInSeconds() == 600){
                    sendWarning(warning.createMinuteWarning("10"));
                }
                if(timeKeeper.timeLeftInSeconds() == 300)
                {
                    sendWarning(warning.createMinuteWarning("5"));
                }
                if(timeKeeper.timeLeftInSeconds() == 60){
                    sendWarning(warning.createMinuteWarning("1"));
                }
            }
            @Override
            public void onFinish(){
                if(isClockedIn){
                    statusReference.setValue(OverstayedStatus);
                }
            }
        }.start();
    }

    void stopTimer(){
        mCountDownTimer.cancel();
        timeKeeper.setTimeLeftInMillis(timeUntilRadiationLimit());
    }

    long timeUntilRadiationLimit(){
        Radiation radiationLevels = new Radiation(30, REACTOR_ROOM, 1);

        double exposurePerSecond = radiationLevels.getUnitExposurePerSecond();

        double exposurePerMiliSecond = exposurePerSecond/1000;

        double timeLeft = radiationLevels.getRadioationLimit()/exposurePerMiliSecond;
        return (long) timeLeft;
    }

    void updateTimer(String timeLeft){
        TextView countdownText = findViewById(R.id.countdownTimer);
        countdownText.setText(timeLeft);
    }

    void sendWarning(Warning warning){
        notificationManager = NotificationManagerCompat.from(this);

        Notification notification = new NotificationCompat.Builder(this, "Channel1")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(warning.getTitle())
                .setContentText(warning.getBody())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();
        notificationManager.notify(1, notification);
    }
    void clockIn(){
        isClockedIn = true;
        TextView textView = findViewById(R.id.status);
        textView.setText("Clocked in");
        myRef.child(currentDate).child(NuclearTechnician).child("Clocked in").push().setValue(currentTime);
       statusReference.setValue("active");
        startTimer();
    }
    void clockOut(){
        isClockedIn = false;
        TextView textView = findViewById(R.id.status);
        textView.setText("Clocked Out");
        myRef.child(currentDate).child(NuclearTechnician).child("Clocked out").push().setValue(currentTime);
        statusReference.setValue("inactive");
        stopTimer();
    }

    void valueEventListener(){
        ValueEventListener statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String technichianStatus = dataSnapshot.getValue().toString();

                if(technichianStatus.equals(OverstayedStatus)) {
                    sendWarning(systemWideWarning);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database error: "+databaseError, Toast.LENGTH_SHORT).show();
            }
        };
        statusReference.addValueEventListener(statusListener);
    }
}
