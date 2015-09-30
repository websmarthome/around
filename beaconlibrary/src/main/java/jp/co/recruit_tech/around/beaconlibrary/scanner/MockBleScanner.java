package jp.co.recruit_tech.around.beaconlibrary.scanner;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import jp.co.recruit_tech.around.beaconlibrary.utils.JSONUtils;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/12.
 */
public class MockBleScanner extends BleScanner {
    private static long INTERVAL_MILLIS = 50;

    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    public MockBleScanner(Context context) {
        this.context = context;
        random.setSeed(new Date().getTime());
    }

    @Override
    public void start(final Listener listener) {
        this.listener = listener;
        stop();

        handler.postDelayed(runnableDelay, INTERVAL_MILLIS);
    }

    @Override
    public void stop() {
        handler.removeCallbacks(runnableDelay);
    }

    private Runnable runnableDelay = new Runnable() {
        @Override
        public void run() {
            onScanInterval();
        }
    };

    private void onScanInterval() {
        if (listener == null) return;

        Date now = new Date();
        try {
            JSONArray jsonArray = JSONUtils.jsonArrayFromAssetFile(context, "json/mock-beacon-data.json");
            List<JSONObject> jsonList = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonArray.length(); i++) {
                if (random.nextInt(2) == 0) continue;

                jsonList.add(jsonArray.getJSONObject(i));
            }

            Collections.shuffle(jsonList, random);
            for (int i = 0; i < jsonList.size(); i++) {
                JSONObject json = jsonList.get(i);
                String url = json.optString("url");
                if (url != null) {
                    UriBeaconData beacon = new UriBeaconData(url, (byte) 100, now);
                    listener.onLeScan(null, 100, beacon);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

}