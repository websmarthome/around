package jp.co.recruit_tech.around.beaconclient.debug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Debug;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

import jp.co.recruit_tech.around.beaconclient.R;
import jp.co.recruit_tech.around.beaconclient.settings.Settings;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.Transaction;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.UrlMetadataDAO;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.service.ServiceCommand;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/12.
 */
public class DebugSettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_screen_debug_settings);

            CheckBoxPreference useMockBleScannerPref = (CheckBoxPreference)findPreference(Settings.PREF_KEY_USE_MOCK_BLE_SCANNER);

            PreferenceScreen beaconHistoryScreen = (PreferenceScreen)findPreference("beacon_history");
            beaconHistoryScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), ScannedBeaconHistoryActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

            PreferenceScreen randomizeMetadataLocation = (PreferenceScreen)findPreference("randomize_metadata_location");
            randomizeMetadataLocation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    randomizeMetadataLocations(getActivity());
                    return true;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
            sharedPreferences.registerOnSharedPreferenceChangeListener(onPreferenceChangeListenter);
        }

        @Override
        public void onPause() {
            super.onPause();
            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(onPreferenceChangeListenter);
        }

        private SharedPreferences.OnSharedPreferenceChangeListener onPreferenceChangeListenter = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                ServiceCommand.ReloadServiceSetting.send(getActivity());
            }
        };

        private static void randomizeMetadataLocations(Context context) {
            final Random random = new Random();
            Transaction<Void> transaction = new Transaction<Void>();
            transaction.run(context, new Transaction.Runner<Void>() {
                @Override
                public Void run(SQLiteDatabase db) {
                    UrlMetadataDAO urlMetadataDAO = new UrlMetadataDAO(db);
                    List<UrlMetadata> metadatas = urlMetadataDAO.findAll();
                    for (UrlMetadata metadata : metadatas) {
                        if (metadata.hasLocation()) {
                            double latitude = metadata.getLatitude();
                            double longitude = metadata.getLongitude();
                            if (random.nextInt(2) == 1) {
                                latitude += random.nextInt(100) * 0.00001;
                                longitude += random.nextInt(100) * 0.00001;
                            } else {
                                latitude -= random.nextInt(100) * 0.00001;
                                longitude -= random.nextInt(100) * 0.00001;
                            }
                            metadata.setLocation(latitude, longitude);
                            urlMetadataDAO.upsert(metadata);
                        }
                    }
                    return null;
                }
            });
            Toast.makeText(context, context.getString(R.string.randomize_metadata_location_done), Toast.LENGTH_SHORT).show();
        }
    }
}
