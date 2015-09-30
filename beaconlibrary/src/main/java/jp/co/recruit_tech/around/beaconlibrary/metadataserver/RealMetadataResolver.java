package jp.co.recruit_tech.around.beaconlibrary.metadataserver;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.List;

/**
 * Metadata Serverから必要な情報を取得する
 *
 * Created by Hideaki on 15/01/29.
 */
public class RealMetadataResolver extends MetadataResolver {
    private static final String TAG = "MetadataResolver";
    private static final String METADATA_URL = "http://atl-phyweb.net/resolve-scan";
    private static RealMetadataResolver instance = null;

    public static RealMetadataResolver getInstance(Context appContext) {
        if (instance == null) {
            instance = new RealMetadataResolver(appContext);
        }
        return instance;
    }

    private RealMetadataResolver(Context context) {
        super(context);
    }

    public void requestUrlMetadatas(List<String> urls) {
        JSONObject requestObject = createUrlMetadataRequestObject(urls);
        JsonObjectRequest request = createUrlMetadataRequest(requestObject);
        getRequestQueue().add(request);
    }

    /**
     * MetadataのRequestを生成する
     */
    private JsonObjectRequest createUrlMetadataRequest(JSONObject requestObject) {
        return new JsonObjectRequest(
                Request.Method.POST,
                METADATA_URL,
                requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonResponse) {
                        onReceiveMetadataResponse(jsonResponse);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.i(TAG, "VolleyError: " + volleyError.toString());
                    }
                }
        );
    }
}
