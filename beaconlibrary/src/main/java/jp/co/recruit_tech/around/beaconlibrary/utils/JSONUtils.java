package jp.co.recruit_tech.around.beaconlibrary.utils;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Hideaki on 15/02/12.
 */
public class JSONUtils {
    public static JSONObject jsonObjectFromAssetFile(Context context, String path) throws JSONException {
        String jsonString = jsonStringFromAssetFile(context, path);
        if (jsonString != null) {
            return new JSONObject(jsonString);
        } else {
            return null;
        }
    }

    public static JSONArray jsonArrayFromAssetFile(Context context, String path) throws JSONException {
        String jsonString = jsonStringFromAssetFile(context, path);
        if (jsonString != null) {
            return new JSONArray(jsonString);
        } else {
            return null;
        }
    }

    public static String jsonStringFromAssetFile(Context context, String path) {
        AssetManager assetManager = context.getResources().getAssets();
        String jsonString = null;
        try {
            InputStream inputStream = assetManager.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            jsonString = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonString;

    }

    public static <T extends Jsonizable> JSONArray toJSONArray(Collection<T> col) throws JSONException {
        JSONArray ary = new JSONArray();
        for (T item : col) {
            ary.put(item.toJSONObject());
        }
        return ary;
    }

    public static <T> List<T> fromJSONArray(JSONArray ary, Unjsonizer<T> unjsonizer) throws JSONException {
        List<T> list = new ArrayList<T>(ary.length());
        for (int i = 0; i < ary.length(); i++) {
            list.add(unjsonizer.fromJSONObject(ary.getJSONObject(i)));
        }
        return list;
    }
}
