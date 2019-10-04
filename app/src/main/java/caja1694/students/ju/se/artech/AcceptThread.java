package caja1694.students.ju.se.artech;

public class AcceptThread {
    //private AcceptThread acceptThread;
    /*
    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread(){
            Log.d(TAG, "AcceptThread: Constructor");
            BluetoothServerSocket tempSocket = null;
            try {
                Log.d(TAG, "AcceptThread: Setting upp serversocket...");
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


        // In synchronize
        if (acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }

    }*/
}
