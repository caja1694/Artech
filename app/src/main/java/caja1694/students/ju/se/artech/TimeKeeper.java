package caja1694.students.ju.se.artech;

import androidx.appcompat.app.AppCompatActivity;

public class TimeKeeper extends AppCompatActivity {

    private long startTimeInMillis;
    private long timeLeftInMillis;
    private Radiation radiation;

    public TimeKeeper(){}

    public TimeKeeper(Radiation radiation) {
        this.radiation = radiation;
        this.startTimeInMillis = calculateNewStartTimeInMillis();
        this.timeLeftInMillis = startTimeInMillis;
    }

    public long getStartTimeInMillis() {
        return startTimeInMillis;
    }

    public void setStartTimeInMillis(long startTimeInMillis) {
        this.startTimeInMillis = startTimeInMillis;
    }

    public long getTimeLeftInMillis() {
        return timeLeftInMillis;
    }

    public void setTimeLeftInMillis(long timeLeftInMillis) {
        this.timeLeftInMillis = timeLeftInMillis;
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
    public long calculateNewStartTimeInMillis(){
        double exposurePerMilliSecond = radiation.getUnitExposurePerMilliSecond();
        double startTime = radiation.getRadioationLimit()/exposurePerMilliSecond;
        return (long) startTime;
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

}