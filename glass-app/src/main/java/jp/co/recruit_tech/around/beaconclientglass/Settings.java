package jp.co.recruit_tech.around.beaconclientglass;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by kusakabe on 15/02/17.
 */
public class Settings {
    private static final String KEY_BLE_MOCK = "bleMock";
    private static final String KEY_METADATA_SERVER_MOCK = "metadataServerMock";

    private static final boolean BLE_MOCK_DEFAULT = false;
    private static final boolean METADATA_SERVER_MOCK_DEFAULT = false;

    private static Settings instance;

    private SharedPreferences sharedPreferences;

    private Settings(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    public static Settings newInstance(Context context) {
        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = new Settings(context);
        return instance;
    }
    public static Settings getInstance() {
        return instance;
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public boolean getBleMockStatus() {
        return sharedPreferences.getBoolean(KEY_BLE_MOCK, BLE_MOCK_DEFAULT);
    }
    public void setBleMockStatus(boolean bleMock) {
        sharedPreferences.edit().putBoolean(KEY_BLE_MOCK, bleMock).commit();
    }

    public boolean getMetadataServerMockStatus() {
        return sharedPreferences.getBoolean(KEY_METADATA_SERVER_MOCK, METADATA_SERVER_MOCK_DEFAULT);
    }
    public void setMetadataServerMockStatus(boolean metaMock) {
        sharedPreferences.edit().putBoolean(KEY_METADATA_SERVER_MOCK, metaMock).commit();
    }
}
