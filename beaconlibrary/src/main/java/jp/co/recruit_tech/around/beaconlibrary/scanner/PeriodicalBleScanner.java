package jp.co.recruit_tech.around.beaconlibrary.scanner;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Hideaki on 15/02/05.
 */
public class PeriodicalBleScanner {
    private BleScanner scanner;
    private BleScanner.Listener listener;
    private int timeoutMillis;
    private int intervalMillis;
    private Timer timeoutTimer;
    private Timer intervalTimer;
    private Handler handler = new Handler(Looper.getMainLooper());

    public PeriodicalBleScanner(Context context, BluetoothAdapter bluetoothAdapter) {
        this.scanner = BleScannerFactory.getInstance().create(context, bluetoothAdapter);
    }

    public void start(int timeoutMillis, int intervalMillis, BleScanner.Listener listener) {
        this.timeoutMillis = timeoutMillis;
        this.intervalMillis = intervalMillis;
        this.listener = listener;
        startScan();
    }

    public void stop() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
        if (intervalTimer != null) {
            intervalTimer.cancel();
            intervalTimer = null;
        }

        scanner.stop();
    }

    private void startScan() {
        stop();

        timeoutTimer = new Timer();
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                scanner.stop();
                timeoutTimer = null;

                intervalTimer = new Timer();
                intervalTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        intervalTimer = null;
                        startScan();
                    }
                }, intervalMillis);
            }
        }, timeoutMillis);

        scanner.start(listener);
    }
}
