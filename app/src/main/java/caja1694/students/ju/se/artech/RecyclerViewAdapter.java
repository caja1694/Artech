package caja1694.students.ju.se.artech;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mDate = new ArrayList<>();
    private ArrayList<String> mHours = new ArrayList<>();
    private ArrayList<String> mRadiationLevel = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(ArrayList<String> mDate, ArrayList<String> mHours, ArrayList<String> mRadiationLevel, Context mContext) {
        this.mDate = mDate;
        this.mHours = mHours;
        this.mRadiationLevel = mRadiationLevel;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.date.setText(mDate.get(position));
        holder.hours.setText(mHours.get(position));
        holder.radiationLevel.setText(mRadiationLevel.get(position));
    }

    @Override
    public int getItemCount() {
        return mDate.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        TextView date;
        TextView hours;
        TextView radiationLevel;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            hours = itemView.findViewById(R.id.hours);
            radiationLevel = itemView.findViewById(R.id.radiationlevel);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
