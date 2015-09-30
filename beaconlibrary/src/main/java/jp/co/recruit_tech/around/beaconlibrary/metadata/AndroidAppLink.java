package jp.co.recruit_tech.around.beaconlibrary.metadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jp.co.recruit_tech.around.beaconlibrary.utils.Jsonizable;
import jp.co.recruit_tech.around.beaconlibrary.utils.Unjsonizer;

/**
 * Metadataに含まれるAndroidのAppLink情報
 *
 * Created by Hideaki on 15/03/17.
 */
public class AndroidAppLink implements Serializable, Jsonizable {
    private final String appName;
    private final String packageName;

    public AndroidAppLink(String appName, String packageName) {
        this.appName = appName;
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public static List<AndroidAppLink> fromResolverJSONArray(JSONArray jsonArray) {
        ArrayList<AndroidAppLink> list = new ArrayList<AndroidAppLink>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                AndroidAppLink appLink = fromResolverJSON(json);
                if (appLink != null) {
                    list.add(appLink);
                }
            }
        } catch (JSONException ex) {
        }

        return list;
    }

    public static AndroidAppLink fromResolverJSON(JSONObject json) {
        try {
            String appName = json.getString("app_name");
            String packageName = json.getString("package");
            AndroidAppLink appLink = new AndroidAppLink(appName, packageName);
            return appLink;
        } catch (JSONException ex) {
            return null;
        }
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("appName", appName);
        json.put("packageName", packageName);
        return json;
    }

    public static final Unjsonizer<AndroidAppLink> unjsonizer = new Unjsonizer<AndroidAppLink>() {
        @Override
        public AndroidAppLink fromJSONObject(JSONObject json) throws JSONException {
            String appName = json.getString("appName");
            String packageName = json.getString("packageName");
            return new AndroidAppLink(appName, packageName);
        }
    };

    public static JSONArray toJSONArray(List<AndroidAppLink> appLinks) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < appLinks.size(); i++) {
            AndroidAppLink appLink = appLinks.get(i);
            jsonArray.put(i, appLink.toJSONObject());
        }
        return jsonArray;
    }

    public static List<AndroidAppLink> fromJSONArray(JSONArray jsonArray) throws JSONException {
        List<AndroidAppLink> appLinks = new ArrayList<AndroidAppLink>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            AndroidAppLink appLink = AndroidAppLink.unjsonizer.fromJSONObject(jsonObject);
            appLinks.add(appLink);
        }
        return appLinks;
    }
}
