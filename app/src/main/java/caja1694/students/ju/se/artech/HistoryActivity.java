package caja1694.students.ju.se.artech;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {


    ThreeColumns_WorkingDayListAdapter adapter = null;


    ListView listView ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        WorkingDay workingDay = new WorkingDay("2016-06-06", "2", "35");
        WorkingDay.workingDayList.add(workingDay);

        adapter = new ThreeColumns_WorkingDayListAdapter(HistoryActivity.this, R.layout.three_columns_list_layout, WorkingDay.workingDayList);

        listView = findViewById(R.id.listViewWorkingDay);
        listView.setAdapter(adapter);




    }




}
