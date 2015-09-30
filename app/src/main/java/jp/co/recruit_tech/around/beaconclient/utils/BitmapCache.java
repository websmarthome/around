package jp.co.recruit_tech.around.beaconclient.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by Hideaki on 15/01/30.
 */
public class BitmapCache implements ImageLoader.ImageCache {
    private LruCache<String, Bitmap> cache;

    public BitmapCache() {
        int maxSize = 10 * 1024 * 1024;
        cache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    @Override
    public Bitmap getBitmap(String url) {
        return cache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        cache.put(url, bitmap);
    }
}
