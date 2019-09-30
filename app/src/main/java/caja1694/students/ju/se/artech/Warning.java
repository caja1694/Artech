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

}
