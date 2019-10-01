package caja1694.students.ju.se.artech;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class Warning extends Application {

    private String title;
    private String body;


    public Warning(){}

    public Warning SystemWideWarning(){
        this.title = "Someone has overstayed";
        this.body = "Someone has overstayed the radiation time limit!";
        return this;
    }
    public Warning createMinuteWarning(String minutes){
        this.title = "" + minutes + " minutes left";
        this.body = "You have "+ minutes + " minutes until you have reached the daily radiation limit";
        return this;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public void sendthisWarning(Context mContext){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

        Notification notification = new NotificationCompat.Builder(mContext, "Channel1")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(this.title)
                .setContentText(this.body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();
        notificationManager.notify(1, notification);
    }


}


