package jp.co.recruit_tech.around.beaconlibrary.scanner;

import android.bluetooth.BluetoothDevice;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/05.
 */
public abstract class BleScanner {
    public interface Listener {
        public void onLeScan(BluetoothDevice device, int rssi, UriBeaconData beaconData);
    }

    protected Listener listener;

    public BleScanner() {
    }

    public abstract void start(Listener listener);

    public abstract void stop();
}
