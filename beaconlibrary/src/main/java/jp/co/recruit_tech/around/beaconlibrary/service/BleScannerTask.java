package jp.co.recruit_tech.around.beaconlibrary.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import jp.co.recruit_tech.around.beaconlibrary.Consts;
import jp.co.recruit_tech.around.beaconlibrary.scanner.BleScanner;
import jp.co.recruit_tech.around.beaconlibrary.scanner.PeriodicalBleScanner;
import jp.co.recruit_tech.around.beaconlibrary.scanner.UriBeaconData;
import jp.co.recruit_tech.around.beaconlibrary.scanner.UriBeaconDataCache;

/**
 * Created by kusakabe on 15/02/21.
 * PeriodicalBleScannerでscanを行い、beaconを検出したらListenerに渡す
 * Bluetoothの設定がおかしいなどで動作できない場合は単に動作しなくなるだけ。
 * その場合、使えるようになったらcreateRestartIntentしてstartServiceしてやると再起動を試みる。
 * 前回scanされて間もないものはListenerに渡さないようにしている。
 */
public class BleScannerTask extends Task {
    public interface Listener {
        /**
         * 新たなBeaconがスキャンされた時に呼ばれる
         */
        public void onScan(int rssi, UriBeaconData uriBeaconData);
    }

    private static final int SCAN_TIMEOUT = 500; // 1s = 1000
    private static final int SCAN_INTERVAL = 1 * 1000; // 1s = 1000

    private Context context;
    private PeriodicalBleScanner periodicalBleScanner;
    private UriBeaconDataCache beaconCache;
    private Listener listener;

    public BleScannerTask(Context context) {
        this.context = context;
        this.beaconCache = new UriBeaconDataCache(Consts.IGNORE_SCANNED_BEACON_PERIOD_MILLIS);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onStart() {
        startScan();
    }

    @Override
    protected void onStop() {
        if (periodicalBleScanner != null) {
            periodicalBleScanner.stop();
            periodicalBleScanner = null;
        }
    }

    private void setupBluetooth() {
        periodicalBleScanner = null;
        BluetoothAdapter bluetoothAdapter = ((BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return;
        }
        periodicalBleScanner = new PeriodicalBleScanner(context, bluetoothAdapter);
    }

    /**
     * スキャンを開始する。
     * すでにスキャン中の時は一旦停止し再開する
     */
    public void startScan() {
        if (periodicalBleScanner != null) {
            periodicalBleScanner.stop();
            periodicalBleScanner = null;
        }
        setupBluetooth();
        if (periodicalBleScanner == null) {
            return;
        }
        periodicalBleScanner.start(SCAN_TIMEOUT, SCAN_INTERVAL, new BleScanner.Listener() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, UriBeaconData uriBeaconData) {
                // キャッシュに残っているBeaconは無視

                Log.d("Scan", uriBeaconData.getUriString());
                if (beaconCache.get(uriBeaconData.getUriString()) != null) {
                    return;
                }
                beaconCache.put(uriBeaconData);
                if (listener != null) {
                    listener.onScan(rssi, uriBeaconData);
                }
            }
        });
    }

    /**
     * 内部保持しているBeaconのキャッシュをクリアする
     */
    public void clearBeaconCache() {
        beaconCache.clear();
    }
}
