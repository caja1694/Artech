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
	final static long ONE_SECOND = 1000;

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
	Button historyAcitivity;

	//Bluetooth
	BluetoothAdapter btAdapter;
	BluetoothDevice btDevice;
	BluetoothConnector btConnector;
	StringBuilder incomingData;

	// Time
	CountDownTimer mCountDownTimer;
	TimeKeeper timeKeeper;
	String currentDate = LocalDate.now().toString();//"2019-09-22";
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
		tester(); // Only for testing sending messages, should  be removed,
		LocalBroadcastManager.getInstance(this).registerReceiver(dataReceiver, new IntentFilter("incomingData"));
		IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(brodCastReceiver2, intentFilter);
		changeActivity();
	}

	private void changeActivity(){
		historyAcitivity = findViewById(R.id.historyButton);
		historyAcitivity.setOnClickListener(new View.OnClickListener() {
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
		try {
			IntentFilter btIntent = new IntentFilter(btAdapter.ACTION_STATE_CHANGED);
			registerReceiver(brodCastReciever1, btIntent);
			if (btAdapter.isEnabled()) {
				startConnection();
			}
		} catch (NullPointerException e){
			Log.d(TAG, "enableBluetooth: " + e.getMessage());
		}
	}
	public void startBTConnection(BluetoothDevice device, UUID uuid){
		Log.d(TAG, "startConnection: Innitialize bt connection");
		btConnector.startClient(device, uuid);
	}

	// Tester, press the button to do something
	private void tester(){
		sendButton = findViewById(R.id.send_message_button);
		sendButton.setText("Send");
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Write code for testing something
				//radiation.setRoomCoefficient(REACTOR_ROOM);
				//reCalculateTimeLeftInMillis();
				send("w");
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
	public void startTimer(){
		mCountDownTimer = new CountDownTimer(timeKeeper.getStartTimeInMillis(), ONE_SECOND) {
			@Override
			public void onTick(long millisUntilFinished) {
				timeKeeper.setTimeLeftInMillis(millisUntilFinished);
				totalTimePast += ONE_SECOND;
				updateTimer(timeKeeper.toString());
				checkForInterValWarning();
				radiation.setTodaysExposure(totalTimePast);
			}
			@Override
			public void onFinish(){
				if(isClockedIn){
					dbManager.setStatus(OverstayedStatus);
					dbManager.setLog(getCurrentTime(), "Overstayed the radiation limit");
					send("w"); // Send warning to BT device
				}
			}
		}.start();
	}
	public void checkForInterValWarning(){
		// Make sure invervall warnings get sent if we jump past a certain time.. With maybe boolean warning 1 / 2 / 3 sent = true / false;
		if(timeKeeper.timeLeftInSeconds() == 600){ warning.createIntervalWarning("10").sendWarning(MainActivity.this); }
		if(timeKeeper.timeLeftInSeconds() == 300){ warning.createIntervalWarning("5").sendWarning(MainActivity.this);  }
		if(timeKeeper.timeLeftInSeconds() == 60) { warning.createIntervalWarning("1").sendWarning(MainActivity.this);  }
	}
	public void stopTimer(){
		mCountDownTimer.cancel();
	}
	public void reCalculateTimeLeftInMillis(){
		long timeLeft =  (long)radiation.getMilliSecondsUntilLimit() - totalTimePast;
		Log.d(TAG, "reCalculateTimeLeftInMillis: timeLeft: " + timeLeft);
		timeKeeper = new TimeKeeper(timeLeft);
		restartTimer();
	}
	public void restartTimer(){
		stopTimer();
		startTimer();
	}
	public void updateTimer(String timeLeft){
		TextView countdownText = findViewById(R.id.countdownTimer);
		countdownText.setText(timeLeft);
	}
	public void clockIn(){
		if(!isClockedIn) {
			isClockedIn = true;
			updateStatusText("Clocked In");
			dbManager.addClockInTime(getCurrentTime());
			startTimer();
		}
	}
	public void clockOut(){
		if(isClockedIn) {
			isClockedIn = false;
			updateStatusText("Clocked Out");
			String shiftTime = timeKeeper.toString(totalTimePast);
			dbManager.setShiftTime(shiftTime);
			dbManager.setRadiationExposure(String.valueOf(radiation.getTodaysExposure()));
			dbManager.addClockOutTime(getCurrentTime());
			stopTimer();
		}
	}
	public void updateStatusText(String text){
		TextView textView = findViewById(R.id.status);
		textView.setText(text);
	}
	public void valueEventListener(){
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
		dbManager.getUserRef().addValueEventListener(statusListener);
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
		Log.d(TAG, "handleIncomingData: data: " + data);
			if(data.length() > 1){
				Log.d(TAG, "handleIncomingData: data > 1: " + Double.valueOf(data));
				radiationLevelChange(Double.valueOf(data));
			}
			switch (data) {
				case "a":
					roomChange(BREAK_ROOM);
					dbManager.setLog(getCurrentTime(), "Entered break room");
					break;
				case "b":
					roomChange(CONTROL_ROOM);
					dbManager.setLog(getCurrentTime(), "Entered control room");
					break;
				case "c":
					roomChange(REACTOR_ROOM);
					dbManager.setLog(getCurrentTime(), "Entered reactor room");
					break;
				case "y":
					suitChange(HAZMAT_SUIT);
					dbManager.setLog(getCurrentTime(), "Suit On");
					break;
				case "n":
					suitChange(NO_SUIT);
					dbManager.setLog(getCurrentTime(), " Suit Off");
					break;
				case "i":
					clockIn();
					break;
				case "o":
					clockOut();
			}

	}
	public void roomChange(double room){
		Log.d(TAG, "roomChange: RoomCoefficiant changed to: " + room);
		if(isClockedIn) {
			radiation.setRoomCoefficient(room);
			reCalculateTimeLeftInMillis();
		}
		else{
			Log.d(TAG, "roomChange: User is not clocked in");
		}
	}
	public void suitChange(double suit){
		Log.d(TAG, "suitChange: ProtectionCoefficiant changed to: " + suit);
		if (isClockedIn){
			radiation.setProtectionCoefficient(suit);
			reCalculateTimeLeftInMillis();
		}
		else{
			Log.d(TAG, "suitChange: User is not clocked in");
		}

	}
	public void radiationLevelChange(Double radiationLevel){
		radiation.setReactorOutputPerSecond(radiationLevel);
		reCalculateTimeLeftInMillis();
	}

	public String getCurrentTime(){
		String time = LocalTime.now().toString().substring(0, 8);
		return time;
	}

}