package jp.co.recruit_tech.around.beaconlibrary.debug;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/16.
 */
public class ScannedBeaconHistory {
    private static ScannedBeaconHistory instance = null;

    private List<ScannedBeacon> beacons = new ArrayList<ScannedBeacon>();

    public static ScannedBeaconHistory getInstance(Context context) {
        if (instance == null) {
            instance = new ScannedBeaconHistory();
        }
        return instance;
    }

    public void push(ScannedBeacon beacon) {
        if (beacons.size() > 0) {
            ScannedBeacon lastBeacon = beacons.get(0);
            if (lastBeacon.getBeacon().getUriString().equals(beacon.getBeacon().getUriString())) {
                return;
            }
        }

        beacons.add(0, beacon);
    }

    public ScannedBeacon getAt(int index) {
        return beacons.get(index);
    }

    public int size() {
        return beacons.size();
    }

    public void clear() {
        beacons.clear();
    }
}
