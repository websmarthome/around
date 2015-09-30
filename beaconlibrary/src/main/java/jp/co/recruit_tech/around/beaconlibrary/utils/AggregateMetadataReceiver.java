package jp.co.recruit_tech.around.beaconlibrary.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadataSet;
import jp.co.recruit_tech.around.beaconlibrary.service.Broadcast;

/**
 * Created by kusakabe on 15/02/21.
 */
public abstract class AggregateMetadataReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Broadcast.OnAggregateMetadata.ACTION.equals(action)) {
            onAggregateMetadata(Broadcast.OnAggregateMetadata.getMetadataSet(intent));
            return;
        }
    }

    protected abstract void onAggregateMetadata(UrlMetadataSet urlMetadataSet);

    public static void addIntentFilterAction(IntentFilter intentFilter) {
        intentFilter.addAction(Broadcast.OnAggregateMetadata.ACTION);
    }
}
