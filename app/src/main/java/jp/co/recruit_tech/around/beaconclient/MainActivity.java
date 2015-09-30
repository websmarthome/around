package jp.co.recruit_tech.around.beaconclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import jp.co.recruit_tech.around.beaconclient.debug.DebugSettingsActivity;
import jp.co.recruit_tech.around.beaconclient.nearby.NearbyUrlMetadataActivity;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadataSet;
import jp.co.recruit_tech.around.beaconlibrary.service.BeaconLibraryMainService;
import jp.co.recruit_tech.around.beaconlibrary.service.IBeaconLibraryMainService;
import jp.co.recruit_tech.around.beaconlibrary.service.ServiceCommand;
import jp.co.recruit_tech.around.beaconlibrary.utils.AggregateMetadataReceiver;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int ENABLE_BLUETOOTH_REQUEST = 17;

    private class Receiver extends AggregateMetadataReceiver {
        @Override
        protected void onAggregateMetadata(UrlMetadataSet urlMetadataSet) {
            updateListViewWithUrlMetadataSet(urlMetadataSet);
        }
    }

    private RequestQueue requestQueue;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ListView listView;
    private UrlMetadataListViewAdapter listViewAdapter;
    private SearchView searchView;
    private Receiver receiver;
    private ServiceConnection serviceConnection;
    private IBeaconLibraryMainService serviceInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBluetooth();

        requestQueue = Volley.newRequestQueue(this);

        listViewAdapter = new UrlMetadataListViewAdapter(this, requestQueue);

        listView = (ListView)findViewById(R.id.list);
        listView.setEmptyView(findViewById(android.R.id.empty));
        listView.setAdapter(listViewAdapter);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(itemClickListener);

        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        AggregateMetadataReceiver.addIntentFilterAction(intentFilter);
        registerReceiver(receiver, intentFilter);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceInterface = IBeaconLibraryMainService.Stub.asInterface(service);
                updateListViewFromService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceInterface = null;
            }
        };
        bindService(new Intent(this, BeaconLibraryMainService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Resumeしたときに読む直す
        updateListViewFromService();
    }

    @Override
    protected void onDestroy() {
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void setupBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.bluetooth_error);
            builder.setMessage(R.string.no_bluetooth).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            builder.show();
        } else {
            if (! bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ENABLE_BLUETOOTH_REQUEST) {
            ServiceCommand.StartBleScan.send(this);
        }
    }

    private void updateListViewWithUrlMetadataSet(UrlMetadataSet urlMetadataSet) {
        listViewAdapter.upsertMetadatas(urlMetadataSet);
        listViewAdapter.notifyDataSetChanged();
    }

    private void updateListViewFromService() {
        if (serviceInterface == null) return;

        UrlMetadataSet metadataSet;
        try {
            metadataSet = serviceInterface.getCurrentUrlMetadataSet();
        } catch (RemoteException ex) {
            // ここには来ないはず
            throw new IllegalStateException();
        }
        updateListViewWithUrlMetadataSet(metadataSet);
    }

    private final ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            UrlMetadata metadata = listViewAdapter.getItem(i);

            /*
            Intent intent = SiteDetailActivity.createIntent(MainActivity.this, metadata);
            startActivity(intent);

            */

            Uri uri = Uri.parse(metadata.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);

            intent.setClassName("org.example.mylfc", "org.example.mylfc.ui.MainActivity");

            startActivity(intent);
            overridePendingTransition(R.anim.left_in, R.anim.left_out);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        searchView = new SearchView(this);
        searchView.setOnQueryTextListener(new SearchViewOnQueryListener());
        searchView.setOnCloseListener(new SearchViewOnCloseListener());
        item.setActionView(searchView);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_nearby_metadata) {
            Intent intent = new Intent(this, NearbyUrlMetadataActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_clear_metadata) {
            ServiceCommand.ReloadServiceSetting.send(this);
            clearMetadata();
            return true;
        } else if (id == R.id.action_debug) {
            Intent intent = new Intent(this, DebugSettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void clearMetadata() {
        try {
            if (serviceInterface != null) {
                serviceInterface.clearUrlMetadataSet();
            }
        } catch (RemoteException ex) {
            // ここには来ないはず
            throw new IllegalStateException();
        }
        listViewAdapter.clear();
        Toast.makeText(this, R.string.clear_list_done, Toast.LENGTH_SHORT).show();
    }

    private class SearchViewOnQueryListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextChange(String text) {
            if (TextUtils.isEmpty(text)) {
                listViewAdapter.getFilter().filter("");
            } else {
                listViewAdapter.getFilter().filter(text);
            }
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String text) {
            searchView.clearFocus();
            return true;
        }
    }

    private class SearchViewOnCloseListener implements SearchView.OnCloseListener {
        @Override
        public boolean onClose() {
            searchView.setQuery("", true);
            return false;
        }
    }
}
