package jp.co.recruit_tech.around.beaconlibrary.metadata;

import android.test.AndroidTestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Hideaki on 15/03/17.
 */
public class AndroidAppLinkTest extends AndroidTestCase {
    public void testResolverJSON() {
        try {
            AndroidAppLink appLink;
            List<AndroidAppLink> appLinks;
            JSONObject json;
            JSONArray jsonArray;

            json = new JSONObject();
            json.put("app_name", "app01");
            json.put("package", "com.example.app01");
            appLink = AndroidAppLink.fromResolverJSON(json);
            assertEquals("app01", appLink.getAppName());
            assertEquals("com.example.app01", appLink.getPackageName());

            jsonArray = new JSONArray();
            for (int i = 0; i < 10; i++) {
                json = new JSONObject();
                json.put("app_name", String.format("app%02d", i));
                json.put("package", String.format("com.example.app%02d", i));
                jsonArray.put(i, json);
            }
            appLinks = AndroidAppLink.fromResolverJSONArray(jsonArray);
            assertEquals(10, appLinks.size());
            assertEquals("app00", appLinks.get(0).getAppName());
            assertEquals("com.example.app00", appLinks.get(0).getPackageName());
            assertEquals("app09", appLinks.get(9).getAppName());
            assertEquals("com.example.app09", appLinks.get(9).getPackageName());
        } catch (JSONException ex) {
        }
    }

    public void testJSON() {
        try {
            AndroidAppLink appLink;
            List<AndroidAppLink> appLinks;
            JSONObject json;
            JSONArray jsonArray;

            json = new JSONObject();
            json.put("appName", "app01");
            json.put("packageName", "com.example.app01");
            appLink = AndroidAppLink.unjsonizer.fromJSONObject(json);
            assertEquals("app01", appLink.getAppName());
            assertEquals("com.example.app01", appLink.getPackageName());

            json = appLink.toJSONObject();
            assertEquals("app01", json.optString("appName"));
            assertEquals("com.example.app01", json.opt("packageName"));

            jsonArray = new JSONArray();
            for (int i = 0; i < 10; i++) {
                json = new JSONObject();
                json.put("appName", String.format("app%02d", i));
                json.put("packageName", String.format("com.example.app%02d", i));
                jsonArray.put(i, json);
            }
            appLinks = AndroidAppLink.fromJSONArray(jsonArray);
            assertEquals(10, appLinks.size());
            assertEquals("app00", appLinks.get(0).getAppName());
            assertEquals("com.example.app00", appLinks.get(0).getPackageName());
            assertEquals("app09", appLinks.get(9).getAppName());
            assertEquals("com.example.app09", appLinks.get(9).getPackageName());

            json = appLinks.get(0).toJSONObject();
            assertEquals("app00", json.optString("appName"));
            assertEquals("com.example.app00", json.opt("packageName"));

            json = appLinks.get(9).toJSONObject();
            assertEquals("app09", json.optString("appName"));
            assertEquals("com.example.app09", json.opt("packageName"));
        } catch (JSONException ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }
}
