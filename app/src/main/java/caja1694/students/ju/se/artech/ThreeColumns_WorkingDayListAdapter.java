package caja1694.students.ju.se.artech;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;


public class ThreeColumns_WorkingDayListAdapter extends ArrayAdapter<WorkingDay>{

    private LayoutInflater mInflater;
    private ArrayList<WorkingDay> workingDayList;
    private int mViewResourceId;



    public ThreeColumns_WorkingDayListAdapter(Context context, int textViewResourceId, ArrayList<WorkingDay> workingDayList){
        super(context, textViewResourceId, workingDayList);
        this.workingDayList = workingDayList;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
    }

    @SuppressLint("ViewHolder")
    @NotNull
    public View getView(int position, View convertView, ViewGroup parents){

        convertView = mInflater.inflate(mViewResourceId,null);
        WorkingDay workingDay = workingDayList.get(position);

        if(workingDay != null){
            TextView date=convertView.findViewById(R.id.date);
            TextView hours=convertView.findViewById(R.id.workingHours);
            TextView level=convertView.findViewById(R.id.radlvl);

            date.setText(workingDay.getDate());

            hours.setText(workingDay.getHours());

            level.setText(workingDay.getRadiationLevel());

        }
        else{
            TextView date=convertView.findViewById(R.id.date);
            TextView hours=convertView.findViewById(R.id.workingHours);
            TextView level=convertView.findViewById(R.id.radlvl);

            date.setText("20160202");

            hours.setText("2");

            level.setText("34");

        }

        return convertView;

    }
}
