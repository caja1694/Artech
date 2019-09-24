package caja1694.students.ju.se.artech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Console;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;




public class MainActivity extends AppCompatActivity {
    final static double BREAK_ROOM = 0.1;
    final static double CONTROL_ROOM = 0.5;
    final static double REACTOR_ROOM = 1.6;

    final static String NuclearTechnician = "NuclearTechnician1";

    String currentTime = LocalTime.now().toString();
    String currentDate = LocalDate.now().toString();

    Boolean isClockedIn = false;
    CountDownTimer mCountDownTimer;

    private NotificationManagerCompat notificationManager;

    private long mTimeLeftInMillis = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Button clockInButton = findViewById(R.id.clock_in_button);
        clockInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addClockInTime();
            }
        });

    }
    void addClockInTime(){
        TextView textView = findViewById(R.id.status);
        DatabaseReference myref = FirebaseDatabase.getInstance().getReference();


        if (!isClockedIn) {
            textView.setText("Clocked in");
            myref.child(currentDate).child(NuclearTechnician).child("Clocked in").push().setValue(currentTime);
            myref.child(currentDate).child(NuclearTechnician).child("Status").setValue("active");
            isClockedIn = true;
            startTimer();
        }
        else {
            textView.setText("Clocked Out");
            myref.child(currentDate).child(NuclearTechnician).child("Clocked out").push().setValue(currentTime);
            isClockedIn = false;
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            myRef.child(currentDate).child(NuclearTechnician).child("Status").setValue("inactive");
            stopTimer();
        }
    }

    void startTimer(){
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateTimer();
                System.out.println(millisUntilFinished);
                if(millisToHours(mTimeLeftInMillis) == 0 && millisToMinutes(mTimeLeftInMillis) == 10 && millisToSeconds(mTimeLeftInMillis) == 0){
                    sendWarning("10 Minutes Left", "You only have 10 minutes left until you have reached the critical radiation limit");
                }
                if(millisToHours(mTimeLeftInMillis) == 0 && millisToMinutes(mTimeLeftInMillis) == 5 && millisToSeconds(mTimeLeftInMillis) == 0)
                {
                    sendWarning("5 Minutes Left", "You only have 5 minutes left until you have reached the critical radiation limit");
                }

                if(millisToHours(mTimeLeftInMillis) == 0 && millisToMinutes(mTimeLeftInMillis) == 1 && millisToSeconds(mTimeLeftInMillis) == 0){
                    sendWarning("1 Minutes Left", "You only have 1 minutes left until you have reached the critical radiation limit");
                }
            }
            @Override
            public void onFinish() {
                sendWarning("WARNING", "You have reached the radiation limit for today. Time to check out.");
                if(isClockedIn){
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                    myRef.child(currentDate).child(NuclearTechnician).child("Status").setValue("overstayed");
                }
            }
        }.start();

    }

    void stopTimer(){
        System.out.println("In stop timer");
        mCountDownTimer.cancel();
        mTimeLeftInMillis = timeLeft(30);
    }

    long timeLeft(double R){

        Radiation radiationLevels = new Radiation(30, REACTOR_ROOM, 1);
        double exposurePerSecond = radiationLevels.getUnitExposurePerSecond();
        double exposurePerMiliSecond = exposurePerSecond/1000;
        double timeLeft = 500000/exposurePerMiliSecond;

        System.out.println(timeLeft/(1000*60*60));

        return (long) timeLeft;
    }

    void updateTimer(){
        TextView countdownText = findViewById(R.id.countdownTimer);
        int hours = millisToHours(mTimeLeftInMillis);
        int minutes = millisToMinutes(mTimeLeftInMillis);
        int seconds = millisToSeconds(mTimeLeftInMillis);

        String timeText;
        if(hours < 10){
            timeText = "0" + hours;
        }
        else {
            timeText = "" + hours;
        }
        if(minutes < 10){
            timeText += ":0" + minutes;
        }
        else{
            timeText += ":" + minutes;
        }
        if(seconds < 10){
            timeText += ":0" + seconds;
        }
        else{
            timeText += ":" + seconds;
        }
        countdownText.setText(timeText);
    }


    void sendWarning(String title, String body){

        notificationManager = NotificationManagerCompat.from(this);

        Notification notification = new NotificationCompat.Builder(this, "Channel1")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();
        notificationManager.notify(1, notification);
    }
    int millisToHours(long millis){
        return (int) mTimeLeftInMillis / (60000 * 60);
    }
    int millisToMinutes(long millis){
        return (int) (mTimeLeftInMillis / 60000) % 60;
    }
    int millisToSeconds(long millis){
        return (int) mTimeLeftInMillis % 60000 / 1000;
    }
}
