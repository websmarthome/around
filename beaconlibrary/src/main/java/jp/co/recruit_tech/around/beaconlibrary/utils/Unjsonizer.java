package jp.co.recruit_tech.around.beaconlibrary.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kusakabe on 15/02/21.
 */
public interface Unjsonizer<T> {
    T fromJSONObject(JSONObject json) throws JSONException;
}
