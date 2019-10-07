package caja1694.students.ju.se.artech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

	final static Warning systemWideWarning = new Warning().SystemWideWarning();

	Radiation radiation;

	Warning warning;

	final static String NuclearTechnician = "NuclearTechnician1";
	final static String OverstayedStatus = "OverstayedStatus";

	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	DatabaseManager dbManager;

	Boolean isClockedIn = false;

	// Buttons
	Button reConnectButton;
	Button clockInButton;
	Button sendButton;
	Button historyButton;

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
	long startTime;
	long totalTimePast;


	private final BroadcastReceiver brodCastReciever1 = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(btAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, btAdapter.ERROR);
				switch (state){
					case BluetoothAdapter.STATE_OFF:
						Log.d(TAG, "onReceive: BT STATE OFF");
						reConnectButton.setVisibility(View.VISIBLE);
						Toast.makeText(MainActivity.this,R.string.Bluetooth_off_message, Toast.LENGTH_LONG).show();
						break;
					case BluetoothAdapter.STATE_TURNING_OFF:
						Log.d(TAG, "brodCastReciever1: BT STATE TURNINF OFF");
						break;

					case BluetoothAdapter.STATE_ON:
						Log.d(TAG, "brodCastReciever1: BT STATE ON");
						reConnectButton.setVisibility(View.GONE);
						startConnection();
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

	private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
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
		unregisterReceiver(dataReceiver);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dbManager  = new DatabaseManager(NuclearTechnician, currentDate);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		incomingData = new StringBuilder();
		radiation = new Radiation(30, BREAK_ROOM, NO_SUIT);
		timeKeeper = new TimeKeeper((long)radiation.getMilliSecondsUntilLimit());
		startTime = (long)radiation.getMilliSecondsUntilLimit();
		totalTimePast = 0;
		warning = new Warning();

		valueEventListener();
		clockFunction();
		enableBluetooth();
		reConnectBtn();
		sendMessage();
		LocalBroadcastManager.getInstance(this).registerReceiver(dataReceiver, new IntentFilter("incomingData"));
		IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(brodCastReceiver2, intentFilter);
		goToHistoryActivity();
	}

	public void goToHistoryActivity(){
		historyButton = findViewById(R.id.historyButton);
		historyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
				startActivity(intent);
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
	public void enableBluetooth(){
		int REQUEST_ENABLE_BT = 1;
		if(btAdapter == null){
			Log.d(TAG, "enableBluetooth: Device doesn't support bluetooth");
		}
		else if(!btAdapter.isEnabled()){
			Log.d(TAG, "enableBluetooth: Enabling bluetooth");
			Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
		}
		IntentFilter btIntent = new IntentFilter(btAdapter.ACTION_STATE_CHANGED);
		registerReceiver(brodCastReciever1, btIntent);
		if(btAdapter.isEnabled()){
			startConnection();
		}
	}
	public void startBTConnection(BluetoothDevice device, UUID uuid){
		Log.d(TAG, "startConnection: Innitialize bt connection");
		btConnector.startClient(device, uuid);
	}

	// Sending the timeStamp.
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

	private void reConnectBtn(){
		reConnectButton = findViewById(R.id.connect_button);
		reConnectButton.setVisibility(View.GONE);
		reConnectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				enableBluetooth();
			}
		});
	}

	void startTimer(){
		mCountDownTimer = new CountDownTimer(timeKeeper.getStartTimeInMillis(), 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				timeKeeper.setTimeLeftInMillis(millisUntilFinished);
				ReCalculateTimeLeftInMillis();

				updateTimer(timeKeeper.toString());
				Log.d(TAG, "onTick: timekeeper.toString: " + timeKeeper.toString());

				if(timeKeeper.timeLeftInSeconds() == 600){ warning.createIntervalWarning("10").sendWarning(MainActivity.this); }

				if(timeKeeper.timeLeftInSeconds() == 300){ warning.createIntervalWarning("5").sendWarning(MainActivity.this);  }

				if(timeKeeper.timeLeftInSeconds() == 60) { warning.createIntervalWarning("1").sendWarning(MainActivity.this);  }

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
		// We should maybe do timeKeeper = new TimeKeeper(radiation.getMillisUntilLimit)
		// And set totalTimePast to 0. This would mean you can only clock in/out once each day.
		// Second option is setting a listener for "new date" and reset and run those 2 lines.
	}

	public void ReCalculateTimeLeftInMillis(){
		totalTimePast += timeKeeper.getTimePastInMillis();
		long timeLeft =  (long)radiation.getMilliSecondsUntilLimit() - totalTimePast;
		Log.d(TAG, "ReCalculateTimeLeftInMillis: timeLeft: " + timeLeft);
		timeKeeper = new TimeKeeper(timeLeft);
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
		Log.d(TAG, "valueEventListener: Listening for status updates in database");
		ValueEventListener statusListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				try {
					String technichianStatus = dataSnapshot.getValue().toString();
					if (technichianStatus.equals(OverstayedStatus)) {
						systemWideWarning.sendWarning(MainActivity.this);
					}
				}
				catch (NullPointerException exception){
					Log.d(TAG, "onDataChange: Listening on a date not yet created?");
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(MainActivity.this, "Database error: "+databaseError, Toast.LENGTH_SHORT).show();
			}
		};
		dbManager.getStatusRef().addValueEventListener(statusListener);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 0){
			reConnectButton.setVisibility(View.VISIBLE);
			// Allow user to re-reConnectBtn
		}
	}

	public void startConnection(){
		btAdapter.cancelDiscovery();
		 Log.d(TAG, "startConnection: Pairing");
		btDevice = btAdapter.getRemoteDevice(HC06);
		try{
				btDevice.createBond();
				btConnector = new BluetoothConnector(MainActivity.this);
				Log.d(TAG, "startConnection: BONDED to: " + btDevice.getName() + " Starting bt connection...");
				startBTConnection(btDevice, uuid);
			} catch(NullPointerException e){
				Log.d(TAG, "startConnection: IOException" + e);
			}

	}

	public void send(String message){
		Log.d(TAG, "MainActivity sending: ''" + message + "''");
		byte [] bytes = message.getBytes(Charset.defaultCharset());
		btConnector.write(bytes);
	}

	public void handleIncomingData(String data){
		switch (data){
			case "a":
				radiation.setRoomCoefficient(BREAK_ROOM);
				Log.d(TAG, "handleIncomingData: BREAK_ROOM");
				ReCalculateTimeLeftInMillis();
				dbManager.setLog(currentTime, "Entered break room");
				break;
			case "b":
				radiation.setRoomCoefficient(CONTROL_ROOM);
				Log.d(TAG, "handleIncomingData: CONTROL_ROOM");
				ReCalculateTimeLeftInMillis();
				dbManager.setLog(currentTime, "Entered control room");
				break;
			case "c":
				radiation.setRoomCoefficient(REACTOR_ROOM);
				Log.d(TAG, "handleIncomingData: REACTOR_ROOM");
				ReCalculateTimeLeftInMillis();
				dbManager.setLog(currentTime, "Entered reactor room");
				break;
			case "x":
				radiation.setRoomCoefficient(BREAK_ROOM);
				Log.d(TAG, "handleIncomingData: BREAK_ROOM");
				ReCalculateTimeLeftInMillis();
				break;
			case "y":
				radiation.setProtectionCoefficient(HAZMAT_SUIT);
				Log.d(TAG, "handleIncomingData: HAZMAT_SUIT");
				ReCalculateTimeLeftInMillis();
				dbManager.setLog(currentTime, "Suit On");
				break;
			case "n":
				radiation.setProtectionCoefficient(NO_SUIT);
				Log.d(TAG, "handleIncomingData: NO_SUIT");
				ReCalculateTimeLeftInMillis();
				dbManager.setLog(currentTime, " Suit Off");
				break;
			case "i":
				clockIn();
				break;
			case "o":
				clockOut();
				break;


		}
	}
}