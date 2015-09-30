package jp.co.recruit_tech.around.beaconlibrary.metadataserver;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.physical_web.physicalweb.UrlShortener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.recruit_tech.around.beaconlibrary.utils.JSONUtils;

/**
 * MetadataResolverのMock
 *
 * Created by Hideaki on 15/01/29.
 */
public class MockMetadataResolver extends MetadataResolver {
    private static MockMetadataResolver instance = null;
    private boolean isAsyncMode = true;
    private Handler handler = new Handler(Looper.getMainLooper());
    private LruCache<String, String> lengthenShortUrlCache;

    public static MockMetadataResolver getInstance(Context appContext) {
        if (instance == null) {
            instance = new MockMetadataResolver(appContext);
        }
        return instance;
    }

    private MockMetadataResolver(Context context) {
        super(context);

        lengthenShortUrlCache = new LruCache<String, String>(100) {
            @Override
            protected int sizeOf(String shortUrl, String realUrl) {
                return 1;
            }
        };
    }

    public void setAsyncMode(boolean isAsyncMode) {
        this.isAsyncMode = isAsyncMode;
    }

    public boolean isAsyncMode() {
        return isAsyncMode;
    }

    @Override
    public void requestUrlMetadatas(final List<String> requestUrls) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Map<String, String> urlMaps = new HashMap<String, String>();
                for (String requestUrl : requestUrls) {
                    String finalUrl = null;
                    if (isShortUrl(requestUrl)) {
                        finalUrl = lengthenShortUrlCache.get(requestUrl);
                        if (finalUrl == null) {
                            finalUrl = UrlShortener.lengthenShortUrl(requestUrl);
                            lengthenShortUrlCache.put(requestUrl, finalUrl);
                        }
                    } else {
                        finalUrl = requestUrl;
                    }

                    if (finalUrl != null) {
                        urlMaps.put(requestUrl, finalUrl);
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        postMetadata(urlMaps);
                    }
                });
            }
        }).start();
    }

    private void postMetadata(Map<String, String> urlMaps) {
        try {
            JSONArray responseMetadataArray = new JSONArray();
            JSONArray metadataArray = JSONUtils.jsonArrayFromAssetFile(getContext(), "json/mock-url-metadata.json");
            for (String requestUrl : urlMaps.keySet()) {
                String finalUrl = urlMaps.get(requestUrl);
                List<JSONObject> matchList = matchMetadata(finalUrl, metadataArray);
                if (matchList.isEmpty()) {
                    JSONObject metadata = new JSONObject();
                    metadata.put("id", requestUrl);
                    metadata.put("url", requestUrl);
                    metadata.put("title", requestUrl);
                    metadata.put("description", "unknown url");
                    metadata.put("icon", "/favicon.ico");
                    responseMetadataArray.put(responseMetadataArray.length(), metadata);
                    continue;
                }
                for (JSONObject metadata : matchList) {
                    metadata.put("id", requestUrl);
                    responseMetadataArray.put(responseMetadataArray.length(), metadata);
                }
            }
            JSONObject response = new JSONObject();
            response.put("metadata", responseMetadataArray);

            if (isAsyncMode) {
                new RequestAsyncTask(response).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                onReceiveMetadataResponse(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<JSONObject> matchMetadata(String finalUrl, JSONArray metadataArray) {
        List<JSONObject> result = new ArrayList<JSONObject>();
        for (int i = 0; i < metadataArray.length(); i++) {
            JSONObject metadata = metadataArray.optJSONObject(i);
            if (metadata != null) {
                String metadataUrl = metadata.optString("url");
                if (finalUrl.equals(metadataUrl)) {
                    result.add(metadata);
                }
            }
        }
        return result;
    }

    private AsyncTask<String, Integer, Long> requestAsyncTask;
    private class RequestAsyncTask extends AsyncTask<String, Integer, JSONObject> {
        private JSONObject jsonObject;

        public RequestAsyncTask(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {

            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            onReceiveMetadataResponse(jsonObject);
        }
    };

    private boolean isShortUrl(String url) {
        Uri uri = Uri.parse(url);
        if (uri == null) return false;

        String host = uri.getHost();
        return (host.equals("goo.gl") ||
                host.equals("bit.ly"));
    }
}
