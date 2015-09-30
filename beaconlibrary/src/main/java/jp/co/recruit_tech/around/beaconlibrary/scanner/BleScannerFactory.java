package jp.co.recruit_tech.around.beaconlibrary.scanner;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

/**
 * Created by Hideaki on 15/02/12.
 */
public class BleScannerFactory {

    private static BleScannerFactory instance = null;

    private boolean isMock;

    private BleScannerFactory() {
        isMock = false;
    }

    public static BleScannerFactory getInstance() {
        if (instance == null) {
            instance = new BleScannerFactory();
        }
        return instance;
    }

    public BleScanner create(Context context, BluetoothAdapter bluetoothAdapter) {
        if (isMock) {
            return new MockBleScanner(context);
        } else {
            return new RealBleScanner(context, bluetoothAdapter);
        }
    }

    public void setMockStatus(boolean isMock) {
        this.isMock = isMock;
    }
    public boolean getMockStatus() {
        return isMock;
    }
}
