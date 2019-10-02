package caja1694.students.ju.se.artech;

import android.bluetooth.BluetoothAdapter;

public class Bluetooth {
    private BluetoothAdapter btAdapter;
    private int REQUEST_ENABLE_CODE;

    public Bluetooth(){
        REQUEST_ENABLE_CODE = 1;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }
}
