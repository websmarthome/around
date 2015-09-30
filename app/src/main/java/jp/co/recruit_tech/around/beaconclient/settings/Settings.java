package jp.co.recruit_tech.around.beaconclient.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Hideaki on 15/02/12.
 */
public class Settings {
    public static final String PREF_KEY_USE_MOCK_BLE_SCANNER = "use_mock_ble_scanner";
    public static final String PREF_KEY_USE_MOCK_METADATA_RESOLVER = "use_mock_metadata_resolver";

    private SharedPreferences prefs;

    public Settings(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean useMockBleScanner() {
        return prefs.getBoolean(PREF_KEY_USE_MOCK_BLE_SCANNER, false);
    }

    public boolean useMockMetadataResolver() {
        return prefs.getBoolean(PREF_KEY_USE_MOCK_METADATA_RESOLVER, false);
    }
}
