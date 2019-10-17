package caja1694.students.ju.se.artech;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BluetoothConnector {
    private final static String TAG = "BluetoothConnector";

    private String appName = "myApp";
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter btAdapter;
    private Context mContext;

    private ConnectThread connectThread;
    private ThreadConnected threadConnected;

    private BluetoothDevice btDevice;
    private UUID deviceUUID;
    ProgressDialog progressDialog;

    public BluetoothConnector(Context context){
        mContext = context;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    // CONNECT THREAD CLASS

    private class ConnectThread extends Thread {
        private BluetoothSocket btSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ConnectThread: Started");
            btDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            Log.d(TAG, "run: Connect thread running...");
            BluetoothSocket tempSocket = null;
            try {
                Log.d(TAG, "run: Trying to createInsecureRFCOM");
                tempSocket = btDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.d(TAG, "run: connect thread failed" + e.getMessage());
            }
            btSocket = tempSocket;

            try {
                Log.d(TAG, "run: Connect thread trying to connect to btSocket...");
                btSocket.connect();
                Log.d(TAG, "run: Connect thread connected");
            } catch (IOException e) {
                try {
                    btSocket.close();
                    Log.d(TAG, "run: Connect thread socket closed: " + e.getMessage());
                } catch (IOException e1) {
                    Log.d(TAG, "run: Connect thread unable to close connection in socket" + e.getMessage());
                }
                Log.d(TAG, "run: Connect thread could not connect to uuid" + deviceUUID);
            } catch (NullPointerException e){
                Log.d(TAG, "run: No bluetooth connection" + e.getMessage());
            }
            connected(btSocket, btDevice);
        }
        public void cancel(){
            try {
                Log.d(TAG, "cancel: Connect thread closing Client socket");
                btSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "cancel: Connect thread Closing failed" + e.getMessage());
            }
        }
    }
    public synchronized void start(){
        Log.d(TAG, "start: synchronized start");
        if(connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }
    }
    public void startClient(BluetoothDevice btDevice, UUID uuid){
        Log.d(TAG, "startClient: Innitiating connect thread");
        progressDialog = ProgressDialog.show(mContext, "Connecting", "Please wait...", true);
        connectThread = new ConnectThread(btDevice, uuid);
        connectThread.start();
    }
    // THREAD CONNECTED CLASS

    public class ThreadConnected extends Thread {
        private BluetoothSocket btSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        private ThreadConnected(BluetoothSocket socket){
            Log.d(TAG, "ThreadConnected: Starting...");
            btSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                progressDialog.dismiss();
            }catch (NullPointerException e){
                Log.d(TAG, "ThreadConnected: Trying to dismiss dialog box, nothing to worry about.");
            }

            try{
                Log.d(TAG, "ThreadConnected: Get input/output stream");
                tempIn = btSocket.getInputStream();
                tempOut = btSocket.getOutputStream();
            } catch (IOException e){
                Log.d(TAG, "threadConnected: Error getting input/output stream" + e);
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            Log.d(TAG, "ThreadConnected run: reading");
            byte[] buffer = new byte[1024];
            int bytes;

            // Listen to InputStream forever
            while (true){
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes, "ASCII");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(incomingMessage);

                    Log.d(TAG, "run: Incoming message: " + incomingMessage + " bytes: " + bytes);
                    Intent intent = new Intent("incomingData");
                    intent.putExtra("incomingMessage", stringBuilder.toString());
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    Log.d(TAG, "run: created intent: " + intent.getExtras());
                } catch (IOException e) {
                    Log.d(TAG, "run: error reading InputStream" + e.getMessage());
                    Intent intent = new Intent("BluetoothError");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    intent.putExtra("Error", e.getMessage());
                    break;
                }
            }
        }

        // Call from main activity
        public void write(byte[] bytes){
            String msg = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Sending message: " + msg);
            try{
                outputStream.write(bytes);
                Log.d(TAG, "write: Message sent: " + msg);
            } catch (IOException e){
                Log.d(TAG, "write: Error writing message: " + e.getMessage());
            }
        }

        // Call from main activity
        public void cancel(){
            try{
                btSocket.close();
            } catch (IOException e){
                Log.d(TAG, "cancel: " + e);
            }
        }
    }
    private void connected(BluetoothSocket socket, BluetoothDevice device){
        Log.d(TAG, "connected: Starting...");
        threadConnected = new ThreadConnected(socket);
        threadConnected.start();
    }
    public void write(byte[] bytes){
        Log.d(TAG, "write: calling threadConnected.write");
        ThreadConnected temp;
        threadConnected.write(bytes);
    }
}
