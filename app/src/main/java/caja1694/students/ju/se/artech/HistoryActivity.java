package caja1694.students.ju.se.artech;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    private ArrayList<String> mDate = new ArrayList<>();
    private ArrayList<String> clockIn = new ArrayList<>();
    private ArrayList<String> clockOut = new ArrayList<>();
    private ArrayList<String> mRadiationLevel = new ArrayList<>();

    private DatabaseManager dbManager;

    private LocalDate currentDate = LocalDate.now();
    private LocalDate startDate;
    private LocalDate endDate;

    List<LocalDate> datesToCheck = new ArrayList<>();


    final static String NuclearTechnician = "NuclearTechnician1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Log.d(TAG, "onCreate: ");
        dbManager = new DatabaseManager(NuclearTechnician, currentDate.toString());

        startDate = LocalDate.parse("2019-09-15");
        endDate = currentDate;

        listDatesToCheck();
        databaseListener();
    }
    public void databaseListener(){
        ValueEventListener logListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + datesToCheck.size());
                for (LocalDate date : datesToCheck){
                    try{
                        String clockInTime = dataSnapshot.child(date.toString()).child(NuclearTechnician).child("Clocked in").getValue().toString();
                        String radiationExposure = dataSnapshot.child(date.toString()).child(NuclearTechnician).child("Radiation exposure").getValue().toString();
                        String clockOutTime = dataSnapshot.child(date.toString()).child(NuclearTechnician).child("Clocked out").getValue().toString();
                        mDate.add(date.toString());
                        clockIn.add("Clocked in: " + clockInTime);
                        clockOut.add("Clocked out: " + clockOutTime);
                        mRadiationLevel.add("Rad exposure: " + radiationExposure);
                        Log.d(TAG, "onDataChange: " + clockInTime);

                    } catch (NullPointerException e){
                        Log.d(TAG, "onDataChange: " + e.getMessage());
                    }
                }
                initRecyclerViewAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        };
        Log.d(TAG, "databaseListener: ");
        dbManager.getBaseReference().addValueEventListener(logListener);
    }

    private void initRecyclerViewAdapter(){
        Log.d(TAG, "initRecyclerViewAdapter: Recycler view init");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mDate, clockIn, clockOut, mRadiationLevel,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    public void listDatesToCheck(){
        while (!startDate.isAfter(endDate)){
            datesToCheck.add(this.startDate);
            startDate = startDate.plusDays(1);
        }
    }

}
