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

    public DatabaseManager(String userName, String currentDate){
        this.baseReference = FirebaseDatabase.getInstance().getReference();
        this.statusRef = baseReference.child(currentDate).child(userName).child("Status");
        this.clockInReference = baseReference.child(currentDate).child(userName).child("Clocked in");
        this.clockOutReference = baseReference.child(currentDate).child(userName).child("Clocked out");
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
    /*
    public boolean eventListener(final String valueToListenFor){
        final boolean[] valueWasFound = {false};
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("In on datachange");
                if(valueToListenFor.equals(dataSnapshot.getValue().toString())) {
                    valueWasFound[0] = true;                                        // This should be changed to better code...
                    System.out.println(valueWasFound[0]);                           // We get here bur dont return, whole thing should
                }                                                                   // work very differently. Should not be a return function.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        statusRef.addValueEventListener(eventListener);
        System.out.println("returning" + valueWasFound[0]);
        return valueWasFound[0];
    }*/

    public DatabaseReference getStatusRef() {
        return statusRef;
    }
}
