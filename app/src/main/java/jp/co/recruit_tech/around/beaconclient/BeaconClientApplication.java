package jp.co.recruit_tech.around.beaconclient;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import jp.co.recruit_tech.around.beaconclient.service.WearService;
import jp.co.recruit_tech.around.beaconclient.settings.Settings;
import jp.co.recruit_tech.around.beaconlibrary.metadataserver.MetadataResolverFactory;
import jp.co.recruit_tech.around.beaconlibrary.scanner.BleScannerFactory;
import jp.co.recruit_tech.around.beaconlibrary.service.BeaconLibraryMainService;

/**
 * Created by kusakabe on 15/02/17.
 */
public class BeaconClientApplication extends Application {

    private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            applyMockSettings();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        setupBeaconLibrarySettings();
        applyMockSettings();

        startService(new Intent(this, BeaconLibraryMainService.class));
        startService(new Intent(this, WearService.class));
    }

    private void setupBeaconLibrarySettings() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(listener);
    }

    private void applyMockSettings() {
        Settings settings = new Settings(BeaconClientApplication.this);
        BleScannerFactory.getInstance().setMockStatus(settings.useMockBleScanner());
        MetadataResolverFactory.getInstance().setMockStatus(settings.useMockMetadataResolver());
    }
}
