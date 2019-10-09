package caja1694.students.ju.se.artech;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseManager {

    String userName;
    String currentDate;

    public DatabaseManager(String userName, String currentDate){
        this.userName = userName;
        this.currentDate = currentDate;
    }

    public void addClockInTime(String time){
        getUserRef().child("Clocked in").setValue(time);
        setStatus("active");
    }
    public void addClockOutTime(String time){
        getUserRef().child("Clocked out").setValue(time);
        setStatus("inactive");
    }
    public void setStatus(String status){
        getUserRef().child("status").setValue(status);
    }

    public void setLog(String value, String child) {
        getUserRef().child("log").child(child).push().setValue(value);
    }
    public void setShiftTime(String time){
        getUserRef().child("log").child("Shift time").setValue(time);
    }
    public void setRadiationExposure(String radiationExposure){
        getUserRef().child("Radiation exposure").setValue(radiationExposure);
    }

    public DatabaseReference getBaseReference() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public DatabaseReference getUserRef() {
        return FirebaseDatabase.getInstance().getReference().child(currentDate).child(userName);
    }
}
