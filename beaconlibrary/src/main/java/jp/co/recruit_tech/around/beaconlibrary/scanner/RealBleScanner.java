package jp.co.recruit_tech.around.beaconlibrary.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.SystemClock;

import org.uribeacon.beacon.UriBeacon;
import org.uribeacon.scan.compat.ScanRecord;
import org.uribeacon.scan.compat.ScanResult;

import java.util.Date;
import java.util.List;

import jp.co.recruit_tech.around.beaconlibrary.debug.ScannedBeacon;
import jp.co.recruit_tech.around.beaconlibrary.debug.ScannedBeaconHistory;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/12.
 */
public class RealBleScanner extends BleScanner {
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private Parcelable[] scanFilterUuids;

    public RealBleScanner(Context context, BluetoothAdapter bluetoothAdapter) {
        super();
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
        scanFilterUuids = new ParcelUuid[]{UriBeacon.URI_SERVICE_UUID,UriBeacon.DEPRECATED_URI_SERVICE_UUID};
    }

    @Override
    public void start(Listener listener) {
        this.listener = listener;
        bluetoothAdapter.startLeScan(myLeScanCallback);
    }

    @Override
    public void stop() {
        bluetoothAdapter.stopLeScan(myLeScanCallback);
    }

    private BluetoothAdapter.LeScanCallback myLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanBytes) {
            ScanRecord scanRecord = ScanRecord.parseFromBytes(scanBytes);
            if (leScanMatches(scanRecord)) {
                final ScanResult scanResult = new ScanResult(device, scanRecord, rssi, SystemClock.elapsedRealtimeNanos());
                byte[] scanRecordBytes = scanResult.getScanRecord().getBytes();
                final UriBeacon uriBeacon =
                        UriBeacon.parseFromBytes(scanRecordBytes);
                if (uriBeacon == null)
                    return;
                ScannedBeacon scannedBeacon = new ScannedBeacon(uriBeacon, new Date());
                ScannedBeaconHistory.getInstance(context).push(scannedBeacon);
                if (listener != null) {
                    listener.onLeScan(device, rssi, new UriBeaconData(uriBeacon, new Date()));
                }
            }
        }
    };

    private boolean leScanMatches(ScanRecord scanRecord) {
        if (scanFilterUuids == null) {
            return true;
        }
        List services = scanRecord.getServiceUuids();
        if (services != null) {
            for (Parcelable uuid : scanFilterUuids) {
                if (services.contains(uuid)) {
                    return true;
                }
            }
        }
        return false;
    }
}
