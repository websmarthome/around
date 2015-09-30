package jp.co.recruit_tech.around.beaconclientglass;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

import jp.co.recruit_tech.around.beaconclientglass.widget.MessageDialogFragment;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadataSet;
import jp.co.recruit_tech.around.beaconlibrary.service.BeaconLibraryMainService;
import jp.co.recruit_tech.around.beaconlibrary.service.ServiceCommand;
import jp.co.recruit_tech.around.beaconlibrary.service.IBeaconLibraryMainService;
import jp.co.recruit_tech.around.beaconlibrary.utils.AggregateMetadataReceiver;

/**
 * Created by kusakabe on 15/02/17.
 */
public class MainActivity extends Activity {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    private class Adapter extends CardScrollAdapter {
        private List<CardBuilder> list;

        Adapter() {
        }

        public void setList(List<CardBuilder> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return list.get(position).getView(convertView, parent);
        }

        @Override
        public int getPosition(Object item) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == item) {
                    return i;
                }
            }
            return AdapterView.INVALID_POSITION;
        }
    }

    private class Cache implements ImageLoader.ImageCache {
        private LruCache<String, Bitmap> rawCache;

        public Cache() {
            rawCache = new LruCache<String, Bitmap>(50 * 1024 * 1024) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return rawCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            rawCache.put(url, bitmap);
        }
    }

    private class Receiver extends AggregateMetadataReceiver {
        @Override
        protected void onAggregateMetadata(UrlMetadataSet urlMetadataSet) {
            MainActivity.this.onAggregateMetadata(urlMetadataSet);
        }
    }

    private CardScrollView cardScrollView;
    private Adapter adapter;
    private Settings settings;
    private UrlMetadataSet metadataSet;
    private Receiver receiver;
    private ServiceConnection serviceConnection;
    private IBeaconLibraryMainService serviceInterface;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private String lastMetadataUrl;

    // TODO make configurable
    private boolean trackingMode = true;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        ServiceCommand.StartBleScan.send(this);
        ServiceCommand.RestartMetadataResolver.send(this);

        settings = Settings.getInstance();
        setupBluetooth();
        metadataSet = new UrlMetadataSet();

        adapter = new Adapter();
        updateCardList();
        cardScrollView = new CardScrollView(this);
        cardScrollView.setAdapter(adapter);
        cardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.this.onItemClick(position);
            }
        });
        setContentView(cardScrollView);

        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        AggregateMetadataReceiver.addIntentFilterAction(intentFilter);
        registerReceiver(receiver, intentFilter);

        requestQueue = Volley.newRequestQueue(this);
        imageLoader = new ImageLoader(requestQueue, new Cache());

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceInterface = IBeaconLibraryMainService.Stub.asInterface(service);
                try {
                    metadataSet = serviceInterface.getCurrentUrlMetadataSet();
                } catch (RemoteException ex) {
                    // ここには来ないはず
                    throw new IllegalStateException();
                }
                onAggregateMetadata(metadataSet);
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
        cardScrollView.activate();
    }

    @Override
    protected void onPause() {
        cardScrollView.deactivate();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        updateMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateMenu(menu);
        return true;
    }

    private void updateMenu(Menu menu) {
        boolean bleMock = settings.getBleMockStatus();
        boolean metaMock = settings.getMetadataServerMockStatus();
        menu.findItem(R.id.turn_off_ble_mock).setVisible(bleMock);
        menu.findItem(R.id.turn_on_ble_mock).setVisible(!bleMock);
        menu.findItem(R.id.turn_off_meta_mock).setVisible(metaMock);
        menu.findItem(R.id.turn_on_meta_mock).setVisible(!metaMock);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.turn_off_ble_mock:
            case R.id.turn_on_ble_mock:
                boolean bleMock = !settings.getBleMockStatus();
                settings.setBleMockStatus(bleMock);
                MessageDialogFragment.show(getFragmentManager(),
                        getString(bleMock ? R.string.message_menu_on_ble_mock : R.string.message_menu_off_ble_mock),
                        null,
                        1000);
                playSound(Sounds.SUCCESS);
                restart();
                return true;
            case R.id.turn_off_meta_mock:
            case R.id.turn_on_meta_mock:
                boolean metaMock = !settings.getMetadataServerMockStatus();
                settings.setMetadataServerMockStatus(metaMock);
                MessageDialogFragment.show(getFragmentManager(),
                        getString(metaMock ? R.string.message_menu_on_meta_mock : R.string.message_menu_off_meta_mock),
                        null,
                        1000);
                playSound(Sounds.SUCCESS);
                restart();
                return true;
            case R.id.clear_database:
                try {
                    if (serviceInterface != null) {
                        serviceInterface.clearUrlMetadataSet();
                    }
                } catch (RemoteException ex) {
                    // ここには来ないはず
                    throw new IllegalStateException(ex);
                }
                MessageDialogFragment.show(getFragmentManager(), getString(R.string.message_clear_database), null, 1000);
                playSound(Sounds.SUCCESS);
                restart();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void restart() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                restartRaw();
            }
        }, 1200);
    }
    private void restartRaw() {
        finish();
        Intent intent = new Intent(this, this.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void setupBluetooth() {
        BluetoothAdapter bluetoothAdapter = ((BluetoothManager)this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BLUETOOTH);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                ServiceCommand.StartBleScan.send(this);
                return;
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.error_bluetooth_reject, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            // ここには来ないはず
            throw new IllegalStateException();
        }
    }

    private void onAggregateMetadata(UrlMetadataSet urlMetadataSet) {
        this.metadataSet = urlMetadataSet;
        updateCardList();
    }

    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics result = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(result);
        return result;
    }

    private boolean isCached(String url) {
        DisplayMetrics displayMetrics = getDisplayMetrics();
        return imageLoader.isCached(url, displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    private Bitmap getCacheBitmap(String url) {
        if (!isCached(url)) {
            return null;
        }
        final List<Bitmap> result = new ArrayList<Bitmap>();
        ImageLoader.ImageListener listener = new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                result.add(response.getBitmap());
            }
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        };
        DisplayMetrics displayMetrics = getDisplayMetrics();
        imageLoader.get(url, listener, displayMetrics.widthPixels, displayMetrics.heightPixels);
        if (result.size() != 1) {
            // ここには来ないはず
            throw new IllegalStateException();
        }
        return result.get(0);
    }

    private void requestLoadImage(String url) {
        ImageLoader.ImageListener listener = new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() == null) {
                    return;
                }
                updateCardList();
            }
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        };
        DisplayMetrics displayMetrics = getDisplayMetrics();
        imageLoader.get(url, listener, displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    private void updateCardList() {
        final List<CardBuilder> list = new ArrayList<CardBuilder>();
        list.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText(String.format("beacon %d found", metadataSet.size()))
                .showStackIndicator(true));
        for (int i = 0; i < metadataSet.size(); i++) {
            UrlMetadata item = metadataSet.get(i);
            String iconUrl = item.getIconUrl();
            CardBuilder cardBuilder = new CardBuilder(this, CardBuilder.Layout.TEXT);
            cardBuilder.setText(String.format("%s\n%s", item.getTitle(), item.getDescription()));
            Bitmap cachedImage = getCacheBitmap(iconUrl);
            if (cachedImage != null) {
                cardBuilder.addImage(cachedImage);
            } else {
                requestLoadImage(iconUrl);
            }
            list.add(cardBuilder);
        }

        final boolean hasNew = hasNewMetadata();
        if (hasNew) {
            updateLastMetadataUrl();
        }
        final int lastCardIdx = list.size() - 1;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.setList(list);
                adapter.notifyDataSetChanged();

                if (trackingMode && hasNew && cardScrollView != null) {
                    cardScrollView.animate(lastCardIdx, CardScrollView.Animation.NAVIGATION);
                }
            }
        });
    }

    private boolean hasNewMetadata() {

        if (metadataSet.size() == 0) {
            return false;
        }
        UrlMetadata lastMetadata = metadataSet.get(metadataSet.size() - 1);
        if (lastMetadataUrl == null || !lastMetadata.getUrl().equals(lastMetadataUrl)) {

            return true;
        }
        return false;
    }

    private void updateLastMetadataUrl() {
        UrlMetadata lastMetadata = metadataSet.get(metadataSet.size() - 1);
        lastMetadataUrl = lastMetadata.getUrl();
    }

    private void playSound(int type) {
        ((AudioManager)getSystemService(Context.AUDIO_SERVICE)).playSoundEffect(type);
    }

    private void onItemClick(int position) {
        if (position == 0) {
            openOptionsMenu();
            playSound(Sounds.TAP);
            return;
        }
        UrlMetadata metadata = metadataSet.get(position - 1);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(metadata.getUrl())));
        playSound(Sounds.TAP);
    }
}
