package jp.co.recruit_tech.around.beaconlibrary.service;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import jp.co.recruit_tech.around.beaconlibrary.database.dao.ShortUrlDAO;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.Transaction;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.UrlMetadataDAO;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadataSet;
import jp.co.recruit_tech.around.beaconlibrary.scanner.UriBeaconData;

/**
 * Created by kusakabe on 15/02/21.
 */
public class BeaconLibraryMainService extends Service {

    private class Binder extends IBeaconLibraryMainService.Stub {
        @Override
        public UrlMetadataSet getCurrentUrlMetadataSet() {
            return aggregateMetadataTask.getCurrentUrlMetadataSet();
        }

        @Override
        public void clearUrlMetadataSet() {
            BeaconLibraryMainService.this.clearUrlMetadataSet();
        }
    }

    private Binder binder;
    private List<Task> taskList;
    private BleScannerTask bleScannerTask;
    private UrlMetadataTask urlMetadataTask;
    private AggregateMetadataTask aggregateMetadataTask;
    private LocationTask locationTask;
    private ServiceCommandReceiver commandIntentReciever;

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new Binder();
        taskList = new ArrayList<Task>();
        bleScannerTask = new BleScannerTask(this);
        bleScannerTask.setListener(new BleScannerTaskListener());
        urlMetadataTask = new UrlMetadataTask(this);
        urlMetadataTask.setListener(new UrlMetadataTaskListener());
        aggregateMetadataTask = new AggregateMetadataTask(this);
        aggregateMetadataTask.setListener(new AggregateMetadataTaskListener());
        locationTask = new LocationTask(this);
        commandIntentReciever = new ServiceCommandReceiver();
        commandIntentReciever.setListener(new CommandIntentReceiverListener());
        taskList.add(bleScannerTask);
        taskList.add(urlMetadataTask);
        taskList.add(aggregateMetadataTask);
        taskList.add(locationTask);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        refreshTaskStatus();
        if (intent != null) {
            commandIntentReciever.onStartCommand(intent);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        for (Task task : taskList) {
            task.stop();
        }
        super.onDestroy();
    }

    private void refreshTaskStatus() {
        bleScannerTask.start();
        urlMetadataTask.start();
        aggregateMetadataTask.start();
        locationTask.start();
    }

    private class BleScannerTaskListener implements BleScannerTask.Listener {
        @Override
        public void onScan(int rssi, UriBeaconData uriBeaconData) {
            urlMetadataTask.resolveUrl(uriBeaconData);
            Broadcast.OnScanUriBeacon.send(BeaconLibraryMainService.this, rssi, uriBeaconData);
        }
    }

    private class AggregateMetadataTaskListener implements AggregateMetadataTask.Listener {
        @Override
        public void onAggregate(UrlMetadataSet metadataSet) {
            Broadcast.OnAggregateMetadata.send(BeaconLibraryMainService.this, metadataSet);
        }
    }

    private class UrlMetadataTaskListener implements UrlMetadataTask.Listener {
        @Override
        public void onResolveUrl(UrlMetadata metadata) {
            aggregateMetadataTask.upsertMetadata(metadata);
            Broadcast.OnResolveUrl.send(BeaconLibraryMainService.this, metadata);
        }
    }

    private void clearUrlMetadataSet() {
        Transaction<Void> transaction = new Transaction<Void>();
        transaction.run(BeaconLibraryMainService.this, new Transaction.Runner<Void>() {
            @Override
            public Void run(SQLiteDatabase db) {
                UrlMetadataDAO urlMetadataDAO = new UrlMetadataDAO(db);
                urlMetadataDAO.deleteAll();

                ShortUrlDAO shortUrlDAO = new ShortUrlDAO(db);
                shortUrlDAO.deleteAll();
                return null;
            }
        });

        bleScannerTask.clearBeaconCache();
        aggregateMetadataTask.clearMetadataSet();
    }

    private class CommandIntentReceiverListener implements ServiceCommandReceiver.Listener {
        @Override
        public void onRequestStartBleScan() {
            bleScannerTask.startScan();
        }

        @Override
        public void onRequestClearScannedBeaconCache() {
            bleScannerTask.clearBeaconCache();
        }

        @Override
        public void onRequestRestartMetadataResolver() {
            urlMetadataTask.restartMetadataResolver();
        }
    }
}
