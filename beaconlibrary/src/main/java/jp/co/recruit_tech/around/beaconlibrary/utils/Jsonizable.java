package jp.co.recruit_tech.around.beaconlibrary.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kusakabe on 15/02/21.
 */
public interface Jsonizable {
    JSONObject toJSONObject() throws JSONException;
}
