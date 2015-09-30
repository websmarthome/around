package jp.co.recruit_tech.around.beaconlibrary.scanner;

import android.util.LruCache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.recruit_tech.around.beaconlibrary.Consts;

/**
 * BeaconDataのLRUをキャッシュ
 *
 * Created by Hideaki on 15/03/11.
 */
public class UriBeaconDataCache {
    private LruCache<String, UriBeaconData> cache;
    private long cachePeriod;

    public UriBeaconDataCache(long cachePeriod) {
        cache = new LruCache<String, UriBeaconData>(100) {
            @Override
            protected int sizeOf(String key, UriBeaconData value) {
                return 1;
            }
        };
        this.cachePeriod = cachePeriod;
    }

    public void put(UriBeaconData beacon) {
        cache.put(beacon.getUriString(), beacon);
    }

    public UriBeaconData get(String uri) {
        UriBeaconData beacon = cache.get(uri);

        if (beacon != null) {
            //
            // 期限切れものものはキャッシュから削除しnullを返す
            //
            Date now = new Date();
            if (now.getTime() - beacon.getScannedAt().getTime() > cachePeriod) {
                cache.remove(uri);
                return null;
            }
        }
        return beacon;
    }

    public void clear() {
        cache.evictAll();
    }
}
