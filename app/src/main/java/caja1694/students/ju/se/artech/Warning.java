package caja1694.students.ju.se.artech;

import androidx.core.app.NotificationManagerCompat;

public class Warning {

    private String title;
    private String body;
    private NotificationManagerCompat notificationManager;

    public Warning(){}

    public Warning SystemWideWarning(){
        this.title = "Someone has overstayed";
        this.body = "Someone has overstayed the radiation time limit!";
        return this;
    }

    public Warning TenMinuteWarning(){
        this.title = "10 minutes left";
        this.body = "You have 10 minutes until you have reached the daily radiation limit";
        return this;
    }
    public Warning FiveMinuteWarning(){
        this.title = "5 minutes left";
        this.body = "You have 5 minutes until you have reached the daily radiation limit";
        return this;
    }
    public Warning OneMinuteWarning(){
        this.title = "1 minutes left";
        this.body = "You have 1 minutes until you have reached the daily radiation limit";
        return this;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

}
