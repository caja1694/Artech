package caja1694.students.ju.se.artech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    Boolean isClockedIn = false;
    CountDownTimer mCountDownTimer;

    private NotificationManagerCompat notificationManager;
    private long mTimeLeftInMillis = timeLeft(30);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

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

        String currentTime = LocalTime.now().toString();
        String currentDate = LocalDate.now().toString();

        if (!isClockedIn) {
            textView.setText("Clocked in");
            myref.child(currentDate).child("Clocked in").push().setValue(currentTime);
            isClockedIn = true;
            startTimer();
        }
        else {
            textView.setText("Clocked Out");
            myref.child(currentDate).child("Clocked out").push().setValue(currentTime);
            isClockedIn = false;
            stopTimer();
        }

    }

    void startTimer(){
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateTimer();
                System.out.println("Counting down");
            }
            @Override
            public void onFinish() {
                sendWarning();
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
        int hours = (int) mTimeLeftInMillis / (60000 * 60);
        int minutes = (int) (mTimeLeftInMillis / 60000) % 60;
        int seconds = (int) mTimeLeftInMillis % 60000 / 1000;

        String timeText;

        timeText = "" + hours;

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
    void sendWarning(){

        String title = "WARNING";
        String body = "You have reached the radiation limit for today. Time to check out.";

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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel1";
            String description = "Warning";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("Channel1", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
