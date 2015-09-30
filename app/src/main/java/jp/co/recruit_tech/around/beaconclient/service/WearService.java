package jp.co.recruit_tech.around.beaconclient.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Vibrator;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;

import jp.co.recruit_tech.around.beaconclient.sitedetail.SiteDetailActivity;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadataSet;
import jp.co.recruit_tech.around.beaconlibrary.service.BeaconLibraryMainService;
import jp.co.recruit_tech.around.beaconlibrary.service.IBeaconLibraryMainService;
import jp.co.recruit_tech.around.beaconlibrary.utils.AggregateMetadataReceiver;

/**
 * Created by kusakabe on 15/03/14.
 */
public class WearService extends Service {

    private static final String DATA_API_PATH = "/DataApiPath";
    private static final String DATA_API_METADATA_KEY = "MetadataKey";
    private static final String MESSAGE_API_START_ACTIVITY_PATH = "/StartActivity";

    private class Receiver extends AggregateMetadataReceiver {
        @Override
        protected void onAggregateMetadata(UrlMetadataSet urlMetadataSet) {
            updateUrlMetadataSet(urlMetadataSet);
        }
    }

    private Receiver receiver;
    private GoogleApiClient googleApiClient;
    private UrlMetadataSet urlMetadataSet;
    private ServiceConnection serviceConnection;
    private IBeaconLibraryMainService serviceInterface;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        AggregateMetadataReceiver.addIntentFilterAction(intentFilter);
        registerReceiver(receiver, intentFilter);

        googleApiClient = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Wearable.MessageApi.addListener(googleApiClient, new MessageApi.MessageListener() {
                        @Override
                        public void onMessageReceived(MessageEvent messageEvent) {
                            if (!MESSAGE_API_START_ACTIVITY_PATH.equals(messageEvent.getPath())) {
                                return;
                            }
                            String url = new String(messageEvent.getData());
                            onRequestStartActivity(url);
                        }
                    });
                }

                @Override
                public void onConnectionSuspended(int i) {
                }
            }).build();
        googleApiClient.connect();

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceInterface = IBeaconLibraryMainService.Stub.asInterface(service);
                try {
                    updateUrlMetadataSet(serviceInterface.getCurrentUrlMetadataSet());
                } catch (RemoteException e) {
                    // ここには来ないはず
                    throw new IllegalStateException();
                }
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceInterface = null;
            }
        };
        bindService(new Intent(this, BeaconLibraryMainService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        unbindService(serviceConnection);
        googleApiClient.disconnect();
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void onRequestStartActivity(String url) {
        if (urlMetadataSet == null) {
            return;
        }
        for (int i = 0; i < urlMetadataSet.size(); i++) {
            UrlMetadata urlMetadata = urlMetadataSet.get(i);
            if (urlMetadata.getUrl().equals(url)) {
                ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
                Intent intent = SiteDetailActivity.createIntent(this, urlMetadata);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return;
            }
        }
    }

    private void updateUrlMetadataSet(UrlMetadataSet urlMetadataSet) {
        this.urlMetadataSet = urlMetadataSet;
        if (!googleApiClient.isConnected()) {
            return;
        }
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DATA_API_PATH);
        try {
            putDataMapRequest.getDataMap().putString(DATA_API_METADATA_KEY, urlMetadataSet.toJSONObject().toString());
        } catch (JSONException e) {
            // ここには来ないはず
            throw new IllegalStateException();
        }
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest());
    }
}
