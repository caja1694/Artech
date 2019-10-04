package caja1694.students.ju.se.artech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


@TargetApi(Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
	final static String TAG = "MainActivity";

	final static String JacobsMacbook = "98:01:A7:BE:EF:4E";
	final static String HC06 = "98:D3:81:FD:46:DA";
	final static String FabiansIphone = "74:B5:87:A9:48:68";
	final static String DennisAndroid = "7C:A1:77:72:BE:42";
	final static String MariasIphone = "A4:D9:31:4A:6D:F0";

	final static double BREAK_ROOM = 0.1;
	final static double CONTROL_ROOM = 0.5;
	final static double REACTOR_ROOM = 1.6;
	final static double NO_SUIT = 1;
	final static double HAZMAT_SUIT = 5;

	Radiation radiation;

	final static Warning systemWideWarning = new Warning().SystemWideWarning();
	Warning warning = new Warning();

	final static String NuclearTechnician = "NuclearTechnician1";
	final static String OverstayedStatus = "OverstayedStatus";

	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	DatabaseManager dbManager;

	Boolean isClockedIn = false;

	// Buttons
	Button connectButton;
	Button clockInButton;
	Button startButton;
	Button sendButton;

	//Bluetooth
	BluetoothAdapter btAdapter;
	BluetoothDevice btDevice;
	BluetoothConnector btConnector;
	StringBuilder incomingData;

	// Time
	CountDownTimer mCountDownTimer;
	TimeKeeper timeKeeper;
	String currentTime = LocalTime.now().toString();
	String currentDate = LocalDate.now().toString();


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

	BroadcastReceiver dataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive dataReceiver: creating message");
			String text = intent.getStringExtra("incomingMessage");
			Log.d(TAG, "onReceive dataReceiver: msg:" + text);
			incomingData.append(text);
			handleIncomingData(text);
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

		dbManager  = new DatabaseManager(NuclearTechnician, currentDate);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		incomingData = new StringBuilder();
		radiation = new Radiation(30, BREAK_ROOM, NO_SUIT);
		timeKeeper = new TimeKeeper(calculateStartTimeInMillis());

		valueEventListener();
		clockFunction();
		connect();
		starter();
		sendMessage();
		LocalBroadcastManager.getInstance(this).registerReceiver(dataReceiver, new IntentFilter("incomingData"));
		IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(brodCastReceiver2, intentFilter);
	}
	public void startBTConnection(BluetoothDevice device, UUID uuid){
		Log.d(TAG, "startConnection: Innitialize bt connection");
		btConnector.startClient(device, uuid);


	}
	public void startConnection(){
		//uuid = btDevice.getUuids()[0].getUuid();
		startBTConnection(btDevice, uuid);
	}
	private void starter(){
		startButton = findViewById(R.id.start_button);
		startButton.setText("start");
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pair();
			}
		});
	}
	private void sendMessage(){
		sendButton = findViewById(R.id.send_message_button);
		sendButton.setText("Send");
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				send(timeKeeper.toString());
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
				calculateTimeLeftInMillis();

				updateTimer(timeKeeper.toString());
				Log.d(TAG, "onTick: timekeeper.toString: " + timeKeeper.toString());

				if(timeKeeper.timeLeftInSeconds() == 600){ warning.createMinuteWarning("10").sendthisWarning(MainActivity.this); }

				if(timeKeeper.timeLeftInSeconds() == 300){ warning.createMinuteWarning("5").sendthisWarning(MainActivity.this);  }

				if(timeKeeper.timeLeftInSeconds() == 60) { warning.createMinuteWarning("1").sendthisWarning(MainActivity.this);  }

				// Update LCD time.
				/*
				if(btConnector != null){
					send(timeKeeper.toString());
				}*/
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
		calculateStartTimeInMillis();
	}
	public long calculateStartTimeInMillis(){
		double exposurePerMiliSecond = radiation.getUnitExposurePerMilliSecond();
		double startTime = radiation.getRadioationLimit()/exposurePerMiliSecond;
		return (long) startTime;
	}

	public void calculateTimeLeftInMillis(){
		double exposurePerMiliSecond = radiation.getUnitExposurePerMilliSecond();
		double startTime = radiation.getRadioationLimit()/exposurePerMiliSecond;
		long timePast = (long) startTime - timeKeeper.getTimeLeftInMillis();
		long timeLeft = (long) startTime - timePast;
		timeKeeper.setTimeLeftInMillis(timeLeft);
	}


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
						systemWideWarning.sendthisWarning(MainActivity.this);
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
		btDevice = btAdapter.getRemoteDevice(HC06);

		Log.d(TAG, "pair: btDevice: " + btDevice);
		if (btDevice != null) {
			try {
				Log.d(TAG, "pair: in if: " + btDevice.getName());
				btDevice.createBond();
				Log.d(TAG, "pair: device UUID" + btDevice.getUuids()[0].toString());
				btConnector = new BluetoothConnector(MainActivity.this);
				Log.d(TAG, "pair: BONDED = " + (btDevice.getBondState() == BluetoothDevice.BOND_BONDED) + btDevice.getName());
			}
		catch(NullPointerException e){
				Log.d(TAG, "pair: IOException" + e);
			}
		}
		startConnection();

	}

	public void send(String message){
		Log.d(TAG, "MainActivity send:");
		byte [] bytes = message.getBytes(Charset.defaultCharset());
		btConnector.write(bytes);
	}
	public void recieveMessage(){
		Log.d(TAG, "recieveMessage: Incomingdata: " + incomingData);
	}
	public void handleIncomingData(String data){
		switch (data){
			case "a":
				radiation.setRoomCoefficient(BREAK_ROOM);
				Log.d(TAG, "handleIncomingData: BREAK_ROOM");
				calculateTimeLeftInMillis();
				break;
			case "b":
				radiation.setRoomCoefficient(CONTROL_ROOM);
				Log.d(TAG, "handleIncomingData: CONTROL_ROOM");
				calculateTimeLeftInMillis();
				break;
			case "c":
				radiation.setRoomCoefficient(REACTOR_ROOM);
				Log.d(TAG, "handleIncomingData: REACTOR_ROOM");
				calculateTimeLeftInMillis();
				break;
			case "x":
				radiation.setRoomCoefficient(BREAK_ROOM);
				Log.d(TAG, "handleIncomingData: BREAK_ROOM");
				calculateTimeLeftInMillis();
				break;
			case "y":
				radiation.setProtectionCoefficient(HAZMAT_SUIT);
				Log.d(TAG, "handleIncomingData: HAZMAT_SUIT");
				calculateTimeLeftInMillis();
				break;
			case "n":
				radiation.setProtectionCoefficient(NO_SUIT);
				Log.d(TAG, "handleIncomingData: NO_SUIT");
				calculateTimeLeftInMillis();
				break;


		}
	}
}
