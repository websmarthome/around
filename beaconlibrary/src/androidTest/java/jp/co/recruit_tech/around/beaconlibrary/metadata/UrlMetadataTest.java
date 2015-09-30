package jp.co.recruit_tech.around.beaconlibrary.metadata;

import android.test.AndroidTestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import jp.co.recruit_tech.around.beaconlibrary.utils.DateUtils;

/**
 * Created by Hideaki on 15/03/17.
 */
public class UrlMetadataTest extends AndroidTestCase {
    public void testJSON() {
        try {
            JSONObject json;
            JSONArray jsonAppLinks;
            JSONObject jsonAppLink;
            UrlMetadata metadata;

            metadata = new UrlMetadata();

            json = new JSONObject();
            json.put("url", "http://example.com/01");
            json.put("title", "title01");
            json.put("description", "desc01");
            json.put("iconUrl", "/favicon01.ico");
            json.put("latitude", 35);
            json.put("longitude", 135);
            json.put("lastScannedAt", DateUtils.simpleFormat(new Date()));
            json.put("lastResolvedAt", DateUtils.simpleFormat(new Date()));

            jsonAppLink = new JSONObject();
            jsonAppLink.put("@type", "android");
            jsonAppLink.put("appName", "app01");
            jsonAppLink.put("packageName", "com.example.app01");
            jsonAppLinks = new JSONArray();
            jsonAppLinks.put(0, jsonAppLink);
            json.put("androidAppLinks", jsonAppLinks);

            metadata =  metadata.unjsonizer.fromJSONObject(json);
            assertEquals("http://example.com/01", metadata.getUrl());
            assertEquals("title01", metadata.getTitle());
            assertEquals("desc01", metadata.getDescription());
            assertEquals("/favicon01.ico", metadata.getIconUrl());
            assertEquals(35.0, metadata.getLatitude());
            assertEquals(135.0, metadata.getLongitude());

            assertEquals(1, metadata.getAndroidAppLinks().size());
            assertEquals("app01", metadata.getAndroidAppLinks().get(0).getAppName());
            assertEquals("com.example.app01", metadata.getAndroidAppLinks().get(0).getPackageName());

            json = metadata.toJSONObject();
            assertEquals("http://example.com/01", json.optString("url"));
            assertEquals("title01", json.optString("title"));
            assertEquals("desc01", json.optString("description"));
            assertEquals("/favicon01.ico", json.optString("iconUrl"));
            assertEquals(35.0, json.optDouble("latitude"));
            assertEquals(135.0, json.optDouble("longitude"));

            jsonAppLinks = json.getJSONArray("androidAppLinks");
            assertEquals(1, jsonAppLinks.length());

            jsonAppLink = jsonAppLinks.getJSONObject(0);
            assertEquals("app01", jsonAppLink.optString("appName"));
            assertEquals("com.example.app01", jsonAppLink.optString("packageName"));
        } catch (JSONException ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }
}
