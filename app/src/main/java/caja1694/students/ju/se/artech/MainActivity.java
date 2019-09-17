package caja1694.students.ju.se.artech;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Boolean isClockedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        final Button clockInButton = findViewById(R.id.clock_in_button);
        clockInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addClockInTime();
            }
        });

    }
    void addClockInTime(){
        TextView textView = findViewById(R.id.status);
        if (!isClockedIn) {
            textView.setText("Clocked in");
            isClockedIn = true;
        }
        else {
            textView.setText("Clocked Out");
            isClockedIn = false;
        }

        //DatabaseReference myref = FirebaseDatabase.getInstance().getReference();

    }
}
