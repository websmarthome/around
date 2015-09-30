package jp.co.recruit_tech.around.beaconlibrary.metadataserver;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.service.LocationTask;

/**
 * Metadata Serverから必要な情報を取得する。継承してReal/Mockを実装
 *
 * Created by Hideaki on 15/01/29.
 */
public abstract class MetadataResolver {
    private static final String TAG = "MetadataResolver";

    public interface RequestCallback {
        public void onUrlMetadataReceived(String requestUrl, UrlMetadata urlMetadata);
        public void onUrlMetadataIconReceived(UrlMetadata urlMetadata);
    }

    private Context context;
    private RequestQueue requestQueue;
    protected RequestCallback metadataResolverCallback;

    protected MetadataResolver(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    protected RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public void setMetadataResolverCallback(RequestCallback metadataResolverCallback) {
        this.metadataResolverCallback = metadataResolverCallback;
    }

    protected JSONObject createUrlMetadataRequestObject(List<String> urls) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray urlJsonArray = new JSONArray();
            for (String url : urls) {
                JSONObject urlJsonObject = new JSONObject();
                urlJsonObject.put("url", url);
                urlJsonArray.put(urlJsonObject);
            }
            jsonObject.put("objects", urlJsonArray);
        } catch (JSONException ex) {
            Log.d(TAG, "error: " + ex);
        }
        return jsonObject;
    }

    /**
     * 複数URLに対するMetadataをリクエストする
     */
    public abstract void requestUrlMetadatas(List<String> urls);

    /**
     * URLに対するMetadataをリクエストする
     */
    public void requestUrlMetadata(String url) {
        List<String> urls = new ArrayList<String>();
        urls.add(url);
        requestUrlMetadatas(urls);
    }

    /**
     * Metadataを受け取った時の処理
     * @param jsonResponse
     */
    protected void onReceiveMetadataResponse(JSONObject jsonResponse) {
        try {
            JSONArray foundMetaData = jsonResponse.getJSONArray("metadata");

            if (foundMetaData.length() > 0) {
                for (int i = 0; i < foundMetaData.length(); i++) {
                    JSONObject jsonUrlMetadata = foundMetaData.getJSONObject(i);
                    UrlMetadata metadata = new UrlMetadata();
                    // TODO: スキャンされた日時はここではなく実際にスキャンされた時にしたい
                    metadata.setLastScannedAt(new Date());
                    metadata.updateFromResolverJSON(jsonUrlMetadata);
                    if (! metadata.isValidData()) continue;

                    String requestUrl = jsonUrlMetadata.optString("id", null);
                    if (requestUrl == null) continue;

                    Location location = LocationTask.getLastLocation();
                    if (location != null) {
                        metadata.setLocation(location.getLatitude(), location.getLongitude());
                    }
                    metadataResolverCallback.onUrlMetadataReceived(requestUrl, metadata);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
