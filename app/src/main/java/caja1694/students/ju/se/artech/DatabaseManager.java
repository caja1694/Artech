package caja1694.students.ju.se.artech;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.SQLOutput;

import androidx.annotation.NonNull;

public class DatabaseManager {

    DatabaseReference baseReference;
    DatabaseReference statusRef;
    DatabaseReference clockInReference;
    DatabaseReference clockOutReference;
    DatabaseReference logReference;

    public DatabaseManager(String userName, String currentDate){
        this.baseReference = FirebaseDatabase.getInstance().getReference();
        this.statusRef = baseReference.child(currentDate).child(userName).child("Status");
        this.clockInReference = baseReference.child(currentDate).child(userName).child("Clocked in");
        this.clockOutReference = baseReference.child(currentDate).child(userName).child("Clocked out");
        this.logReference = baseReference.child(currentDate).child(userName).child("log");
    }
    public void addClockInTime(String time){
        clockInReference.push().setValue(time);
        statusRef.setValue("active");
    }
    public void addClockOutTime(String time){
        clockOutReference.push().setValue(time);
        statusRef.setValue("inactive");
    }
    public void setStatus(String status){
        statusRef.setValue(status);
    }

    public void setLog(String child, String value) {
        logReference.child(value).push().setValue(child);

    }

    public DatabaseReference getStatusRef() {
        return statusRef;
    }
}
