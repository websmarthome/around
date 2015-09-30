package jp.co.recruit_tech.around.beaconclientglass;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

import jp.co.recruit_tech.around.beaconlibrary.metadataserver.MetadataResolverFactory;
import jp.co.recruit_tech.around.beaconlibrary.scanner.BleScannerFactory;
import jp.co.recruit_tech.around.beaconlibrary.service.BeaconLibraryMainService;

/**
 * Created by kusakabe on 15/02/17.
 */
public class BeaconClientGlassApplication extends Application {

    private SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    applyMockSettings();
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        Settings settings = Settings.newInstance(this);
        applyMockSettings();
        settings.registerOnSharedPreferenceChangeListener(listener);
        startService(new Intent(this, BeaconLibraryMainService.class));
    }

    private void applyMockSettings() {
        Settings settings = Settings.getInstance();
        BleScannerFactory.getInstance().setMockStatus(settings.getBleMockStatus());
        MetadataResolverFactory.getInstance().setMockStatus(settings.getMetadataServerMockStatus());
    }
}
