package caja1694.students.ju.se.artech;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.nfc.Tag;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnector {
    private final static String TAG = "BluetoothConnector";

    private String appName = "myApp";
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter btAdapter;
    private Context mContext;

    private AcceptThread acceptThread;
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

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread(){
            Log.d(TAG, "AcceptThread: Constructor");
            BluetoothServerSocket tempSocket = null;
            try {
                Log.d(TAG, "AcceptThread: Settering upp serversocket...");
                tempSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, uuid);
            } catch (IOException e) {
                Log.d(TAG, "AcceptThread: IOException" +e);
            }
            serverSocket = tempSocket;
        }

        public void run() {
            BluetoothSocket btSocket = null;
            Log.d(TAG, "run: Accept thread running...");
            try{
                Log.d(TAG, "run: RFCOM server socket start...");
                btSocket = serverSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection");
                
            } catch (IOException e){
                System.out.println("Error in cancel" + e.getMessage());
            }
            if (btSocket != null){
                connected(btSocket, btDevice);
            }
            Log.d(TAG, "run: End Accept thread");
        }
        public void cancel(){
            try{
                serverSocket.close();
            } catch (IOException e){
                Log.d(TAG, "cancel: cancel failed" + e.getMessage());
            }
        }
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
            //btAdapter.cancelDiscovery();

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
                Log.d(TAG, "run: Connect thread could not connect to uuid");
            }
            connected(btSocket, btDevice);
        }
        public void cancel(){
            try {
                Log.d(TAG, "cancel: Connect thread closing Client socket");
                btSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "cancel: Connect thread Closing falied" + e.getMessage());
            }
        }
    }
    public synchronized void start(){
        Log.d(TAG, "start: synchronized start");
        if(connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }
        if (acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
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

            progressDialog.dismiss();

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
            byte[] buffer = new byte[1024];
            int bytes;

            // Listen to InputStream forever
            while (true){
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "run: Incoming message: " + incomingMessage);
                } catch (IOException e) {
                    Log.d(TAG, "run: error reading InputStream" + e.getMessage());
                    break;
                }
            }
        }

        // Call from main activity
        public void write(byte[] bytes){
            String msg = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Sending message" + msg);
            try{
                outputStream.write(bytes);
                Log.d(TAG, "write: Message sent: " + msg);
            } catch (IOException e){
                Log.d(TAG, "write: Error writing message" + e.getMessage());
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
