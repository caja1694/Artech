package caja1694.students.ju.se.artech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


@TargetApi(Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
	final static String TAG = "MainActivity";

	final static String JacobsMacbook = "98:01:A7:BE:EF:4E";
	final static String HC06 = "98:D3:81:FD:46:DA";

	final static double BREAK_ROOM = 0.1;
	final static double CONTROL_ROOM = 0.5;
	final static double REACTOR_ROOM = 1.6;

	final static Warning systemWideWarning = new Warning().SystemWideWarning();

	Warning warning = new Warning();

	final static String NuclearTechnician = "NuclearTechnician1";
	final static String OverstayedStatus = "OverstayedStatus";

	String currentTime = LocalTime.now().toString();
	String currentDate = LocalDate.now().toString();

	private UUID uuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

	DatabaseManager dbManager = new DatabaseManager(NuclearTechnician, currentDate);

	Boolean isClockedIn = false;
	CountDownTimer mCountDownTimer;

	Button connectButton;
	Button clockInButton;
	Button startButton;

	BluetoothAdapter btAdapter;
	BluetoothDevice btDevice;
	BluetoothConnector btConnector;

	private Context mContext = this;
	TimeKeeper timeKeeper = new TimeKeeper(20000);

	private final BroadcastReceiver brodCastReciever1 = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(btAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, btAdapter.ERROR);
				switch (state){
					case BluetoothAdapter.STATE_OFF:
						Log.d(TAG, "onReceive: BT STATE OFF");
						break;
					case BluetoothAdapter.STATE_TURNING_OFF:
						Log.d(TAG, "brodCastReciever1: BT STATE TURNINF OFF");
						break;

					case BluetoothAdapter.STATE_ON:
						Log.d(TAG, "brodCastReciever1: BT STATE ON");
						pair();
						break;
					case BluetoothAdapter.STATE_TURNING_ON:
						Log.d(TAG, "brodCastReceiver1: BT STATE TURNING ON");
						break;
				}
			}
		}
	};
	private final BroadcastReceiver brodCastReceiver2 = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				BluetoothDevice tmpBtDevice = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
				if(tmpBtDevice.getBondState() == BluetoothDevice.BOND_BONDED){
					Log.d(TAG, "brodCastReceiver3: BONDED to " + tmpBtDevice.getName());
					//btDevice = tmpBtDevice;
				}
				if(tmpBtDevice.getBondState() == BluetoothDevice.BOND_BONDING){
					Log.d(TAG, "brodCastReceiver3: BONDING");
				}
				if(tmpBtDevice.getBondState() == BluetoothDevice.BOND_NONE){
					Log.d(TAG, "brodCastReceiver3: BOND_NONE");
				}
			}
		}
	};

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy: called");
		super.onDestroy();
		unregisterReceiver(brodCastReciever1);
		unregisterReceiver(brodCastReceiver2);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		valueEventListener();
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		clockFunction();
		connect();
		starter();
		IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(brodCastReceiver2, intentFilter);
	}
	public void startBTConnection(BluetoothDevice device, UUID uuid){
		Log.d(TAG, "startConnection: Innitialize bt connection");
		btConnector.startClient(device, uuid);
	}
	public void startConnection(){
		startBTConnection(btDevice, uuid);
	}
	private void starter(){
		startButton = findViewById(R.id.start_button);
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startConnection();
			}
		});
	}
	private void connect(){
		connectButton = findViewById(R.id.connect_button);
		connectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				enableDisableBluetooth();
			}
		});
	}
	private void clockFunction() {
		clockInButton = findViewById(R.id.clock_in_button);
		clockInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isClockedIn) { clockOut(); }
				else { clockIn(); }
			}
		});
	}

	void startTimer(){
		mCountDownTimer = new CountDownTimer(timeKeeper.getStartTimeInMillis(), 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				timeKeeper.setTimeLeftInMillis(millisUntilFinished);
				updateTimer(timeKeeper.toString());
				BluetoothDevice tmpBtDevice = btDevice;

				if(timeKeeper.timeLeftInSeconds() == 600){ warning.createMinuteWarning("10").sendthisWarning(mContext); }

				if(timeKeeper.timeLeftInSeconds() == 300){ warning.createMinuteWarning("5").sendthisWarning(mContext);  }

				if(timeKeeper.timeLeftInSeconds() == 60) { warning.createMinuteWarning("1").sendthisWarning(mContext);  }
				if(tmpBtDevice != null) {
					int state = tmpBtDevice.getBondState();
					switch (state) {
                        case BluetoothDevice.BOND_BONDING:
                            Log.d(TAG, "startDownTimer: BONDING");
                            break;

						case BluetoothDevice.BOND_BONDED:  // CHECK BOND STATUS
							Log.d(TAG, "startDownTimer: BONDED to " + tmpBtDevice.getName());
							break;
							//btDevice = tmpBtDevice;

						case BluetoothDevice.BOND_NONE:
							Log.d(TAG, "brodCastReceiver3: BOND_NONE");
							break;
					}
				}
			}
			@Override
			public void onFinish(){
				if(isClockedIn){
					dbManager.setStatus(OverstayedStatus);
				}
			}
		}.start();
	}

	void stopTimer(){
		mCountDownTimer.cancel();
		timeKeeper.setTimeLeftInMillis(timeUntilRadiationLimit());
	}

	long timeUntilRadiationLimit(){
		Radiation radiationLevels = new Radiation(30, REACTOR_ROOM, 1); // This should be in TimeKeeper
		double exposurePerMiliSecond = radiationLevels.getUnitExposurePerMilliSecond();                       // taking the radiation-obj as
		double timeLeft = radiationLevels.getRadioationLimit()/exposurePerMiliSecond;                         // param and use
		return (long) timeLeft;                                                                               // timeKeeper.timeLEFTInMillis
	}                                                                                                         // to recalculate.

	void updateTimer(String timeLeft){
		TextView countdownText = findViewById(R.id.countdownTimer);
		countdownText.setText(timeLeft);
	}
	void clockIn(){
		isClockedIn = true;
		updateStatusText("Clocked In");
		dbManager.addClockInTime(currentTime);
		startTimer();
	}
	void clockOut(){
		isClockedIn = false;
		updateStatusText("Clocked Out");
		dbManager.addClockOutTime(currentTime);
		stopTimer();
	}
	void updateStatusText(String text){
		TextView textView = findViewById(R.id.status);
		textView.setText(text);
	}

	void valueEventListener(){
		ValueEventListener statusListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				try {
					String technichianStatus = dataSnapshot.getValue().toString();
					if (technichianStatus.equals(OverstayedStatus)) {
						systemWideWarning.sendthisWarning(mContext);
					}
				}
				catch (NullPointerException exception){
					System.out.println("Nullpointer exception in valueEventListener, probably couz there are no clock ins yet on this date");
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(MainActivity.this, "Database error: "+databaseError, Toast.LENGTH_SHORT).show();
			}
		};
		dbManager.getStatusRef().addValueEventListener(statusListener);
	}

	public void enableDisableBluetooth(){
		int REQUEST_ENABLE_BT = 1;

		if(btAdapter == null){
			Log.d(TAG, "enableDisableBluetooth: Device doesn't support bluetooth");
		}
		else if(!btAdapter.isEnabled()){
			Log.d(TAG, "enableDisableBluetooth: Enabelning bluetooth");
			Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);

			IntentFilter btIntent = new IntentFilter(btAdapter.ACTION_STATE_CHANGED);
			registerReceiver(brodCastReciever1, btIntent);
		}
		else if(btAdapter.isEnabled()){
			Log.d(TAG, "enableDisableBluetooth: Disabling bluetooth");
			btAdapter.disable();

			IntentFilter btIntent = new IntentFilter(btAdapter.ACTION_STATE_CHANGED);
			registerReceiver(brodCastReciever1, btIntent);
		}
	}
	public void pair(){
		btAdapter.cancelDiscovery();
		 Log.d(TAG, "pair: Pairing");
		btDevice = btAdapter.getRemoteDevice(JacobsMacbook);

		Log.d(TAG, "pair: btDevice: " + btDevice);
		if (btDevice != null) {
			Log.d(TAG, "pair: in if" + btDevice.getName());
			btDevice.createBond();
			Log.d(TAG, "pair: device UUID" + btDevice.getUuids()[0].toString());
			btConnector = new BluetoothConnector(MainActivity.this);
		}
		try{
			Log.d(TAG, "pair: BONDED = " + (btDevice.getBondState() == BluetoothDevice.BOND_BONDED) + btDevice.getName());
		}
		catch (NullPointerException e){
			Log.d(TAG, "pair: IOException" + e);
		}

	}

	public void send(){
		Log.d(TAG, "MainActivity send:");
		byte [] bytes = "1".getBytes(Charset.defaultCharset());
		btConnector.write(bytes);
	}
}
