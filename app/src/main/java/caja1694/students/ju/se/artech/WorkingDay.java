package caja1694.students.ju.se.artech;

import java.util.ArrayList;

public class WorkingDay {

    private String date;
    private String hours;
    private String radiationLevel;
    static public ArrayList<WorkingDay>workingDayList = new ArrayList<>();


    public WorkingDay(String date, String hours, String radiationLevel){
        this.date = date;
        this.hours = hours;
        this.radiationLevel = radiationLevel;
    }




    public String getDate(){
        return this.date;
    }
    public String getHours(){
        return this.hours;
    }
    public String getRadiationLevel(){
        return this.getRadiationLevel();
    }


}

