package caja1694.students.ju.se.artech;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    private ArrayList<String> mDate = new ArrayList<>();
    private ArrayList<String> mHours = new ArrayList<>();
    private ArrayList<String> mRadiationLevel = new ArrayList<>();

    DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        retrieveFromDatabase();
    }

    private void retrieveFromDatabase(){
        mDate.add("12-22-2019");
        mHours.add("22");
        mRadiationLevel.add("203");

        mDate.add("12-22-2019");
        mHours.add("22");
        mRadiationLevel.add("203");

        mDate.add("12-22-2019");
        mHours.add("22");
        mRadiationLevel.add("203");

        initRecyclerViewAdapter();
    }

    private void initRecyclerViewAdapter(){
        Log.d(TAG, "initRecyclerViewAdapter: Recycler view init");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mDate,mHours, mRadiationLevel,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
