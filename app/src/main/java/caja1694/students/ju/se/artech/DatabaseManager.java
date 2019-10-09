package caja1694.students.ju.se.artech;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseManager {

    DatabaseReference baseReference;
    DatabaseReference userRef;
    DatabaseReference clockInReference;
    DatabaseReference clockOutReference;
    DatabaseReference logReference;
    public DatabaseManager(){
        this.baseReference = FirebaseDatabase.getInstance().getReference();
    }

    public DatabaseManager(String userName, String currentDate){
        this.baseReference = FirebaseDatabase.getInstance().getReference();
        this.userRef = baseReference.child(currentDate).child(userName);
        this.clockInReference = baseReference.child(currentDate).child(userName).child("Clocked in");
        this.clockOutReference = baseReference.child(currentDate).child(userName).child("Clocked out");
        this.logReference = baseReference.child(currentDate).child(userName).child("log");
    }
    public void addClockInTime(String time){
        clockInReference.setValue(time);
        setStatus("active");
    }
    public void addClockOutTime(String time){
        clockOutReference.setValue(time);
        setStatus("inactive");
    }
    public void setStatus(String status){
        userRef.child("status").setValue(status);
    }

    public void setLog(String value, String child) {
        logReference.child(child).push().setValue(value);
    }
    public void setShiftTime(String time){
        logReference.child("Shift time").setValue(time);
    }
    public void setRadiationExposure(String radiationExposure){
        userRef.child("Radiation exposure").setValue(radiationExposure);
    }

    public DatabaseReference getUserRef() {
        return userRef;
    }
}
