package caja1694.students.ju.se.artech;

import androidx.appcompat.app.AppCompatActivity;

public class TimeKeeper extends AppCompatActivity {

    private long startTimeInMillis;
    private long timeLeftInMillis;

    public TimeKeeper(long startTimeInMillis) {
        this.startTimeInMillis = startTimeInMillis;
        this.timeLeftInMillis = startTimeInMillis;
    }

    public long getStartTimeInMillis() {
        return startTimeInMillis;
    }

    public long getTimeLeftInMillis() {
        return timeLeftInMillis;
    }

    public void setTimeLeftInMillis(long timeLeftInMillis) {
        this.timeLeftInMillis = timeLeftInMillis;
    }

    public long getTimePastInMillis(){
        return startTimeInMillis - timeLeftInMillis;
    }

    public int timeLeftInHours() {
        return (int) this.timeLeftInMillis / (60000 * 60);
    }

    public int timeLeftInMinutes() {
        return (int) (this.timeLeftInMillis / 60000);
    }

    public int timeLeftInSeconds() {
        return (int) this.timeLeftInMillis / 1000;
    }

    public String toString(){
        int hours = timeLeftInHours();
        int minutes = timeLeftInMinutes()%60;
        int seconds = timeLeftInSeconds()%60;

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
        return timeText;
    }
    public String toString(long millis){
        int hours = (int) millis / (60000 * 60);
        int minutes = (int) (millis / 60000) %60;
        int seconds = (int) (millis / 1000) %60;
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
        return timeText;
    }

}