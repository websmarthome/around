package jp.co.recruit_tech.around.beaconlibrary.service;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadataSet;
import jp.co.recruit_tech.around.beaconlibrary.scanner.UriBeaconData;

/**
 * ServiceからBroadcastするもの
 *
 * Created by Hideaki on 15/03/10.
 */
public class Broadcast {
    /**
     * URI BeaconがScanされたときのBroadcast
     */
    public static class OnScanUriBeacon {
        public static final String ACTION = "jp.co.recruit_tech.around.beaconlibrary.service.Broadcast.OnScanUriBeacon";
        private static final String KEY_RSSI = "rssi";
        private static final String KEY_URI_BEACON_DATA = "uriBeaconData";

        public static void send(Context context, int rssi, UriBeaconData uriBeaconData) {
            Intent intent = new Intent(ACTION);
            intent.putExtra(KEY_RSSI, rssi);
            intent.putExtra(KEY_URI_BEACON_DATA, uriBeaconData);
            context.sendBroadcast(intent);
        }

        public static int getRssi(Intent intent) {
            return intent.getIntExtra(KEY_RSSI, 0);
        }

        public static UriBeaconData getBeaconData(Intent intent) {
            return (UriBeaconData)intent.getParcelableExtra(KEY_URI_BEACON_DATA);
        }
    }

    /**
     * URLにひもづくMetadataが得られたときのBroadcast
     */
    public static class OnResolveUrl {
        public static final String ACTION = "jp.co.recruit_tech.around.beaconlibrary.service.Broadcast.OnResolveUrl";
        private static final String KEY_METADATA = "metadata";

        public static void send(Context context, UrlMetadata urlMetadata) {
            Intent intent = new Intent(ACTION);
            intent.putExtra(KEY_METADATA, (Parcelable)urlMetadata);
            context.sendBroadcast(intent);
        }

        public static UrlMetadata getMetadata(Intent intent) {
            return (UrlMetadata)intent.getParcelableExtra(KEY_METADATA);
        }
    }

    /**
     * URI Metadataが収集、更新された時のBroadcast
     */
    public static class OnAggregateMetadata {
        public static final String ACTION = "jp.co.recruit_tech.around.beaconlibrary.service.Broadcast.OnAggregateMetadata";
        private static final String KEY_METADATA_SET = "metadataSet";

        public static void send(Context context, UrlMetadataSet metadataSet) {
            Intent intent = new Intent(ACTION);
            try {
                intent.putExtra(KEY_METADATA_SET, metadataSet.toJSONObject().toString());
            } catch (JSONException e) {
                // ここには来ないはず
                throw new IllegalStateException();
            }
            context.sendBroadcast(intent);
        }

        public static UrlMetadataSet getMetadataSet(Intent intent) {
            try {
                return UrlMetadataSet.unjsonizer.fromJSONObject(new JSONObject(intent.getStringExtra(KEY_METADATA_SET)));
            } catch (JSONException e) {
                // ここには来ないはず
                throw new IllegalStateException();
            }
        }
    }
}
