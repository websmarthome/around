package jp.co.recruit_tech.around.beaconlibrary.metadata;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jp.co.recruit_tech.around.beaconlibrary.utils.DateUtils;
import jp.co.recruit_tech.around.beaconlibrary.utils.Jsonizable;
import jp.co.recruit_tech.around.beaconlibrary.utils.Unjsonizer;

/**
 * メタデータを示す
 *
 * Created by Hideaki on 15/01/29.
 */
public class UrlMetadata implements Parcelable, Jsonizable {
    private String url;
    private String title;
    private String description;
    private String iconUrl;
    private Double latitude;
    private Double longitude;
    private Date lastScannedAt;
    private Date lastResolvedAt;
    private ArrayList<AndroidAppLink> androidAppLinks = new ArrayList<AndroidAppLink>();

    /**
     * Resolverから得られたJSONで情報を更新する
     */
    public void updateFromResolverJSON(JSONObject json) {
        url = json.optString("url", null);
        title = json.optString("title", "");
        description = json.optString("description", "");
        iconUrl = json.optString("icon", "/favicon.ico");
        if (json.has("latitude") && json.has("longitude")) {
            latitude = json.optDouble("latitude", 0);
            longitude = json.optDouble("longitude", 0);
        } else {
            clearLocation();
        }

        lastResolvedAt = new Date();
        iconUrl = buildIconUrl(iconUrl, url);

        JSONObject jsonLd = json.optJSONObject("json-ld");
        if (jsonLd != null) {
            String type = jsonLd.optString("@type");
            if (type != null && type.equals("AppLinks")) {
                JSONArray android = jsonLd.optJSONArray("android");
                androidAppLinks.clear();
                androidAppLinks.addAll(AndroidAppLink.fromResolverJSONArray(android));
            }
        }
    }

    public static UrlMetadata fromDBCursor(Cursor cursor) {
        UrlMetadata metadata = new UrlMetadata();
        metadata.url = cursor.getString(cursor.getColumnIndex("url"));
        metadata.title = cursor.getString(cursor.getColumnIndex("title"));
        metadata.description = cursor.getString(cursor.getColumnIndex("description"));
        metadata.iconUrl = cursor.getString(cursor.getColumnIndex("icon_url"));
        metadata.latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
        metadata.longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
        metadata.lastScannedAt = new Date(cursor.getLong(cursor.getColumnIndex("last_scanned_at")));
        metadata.lastResolvedAt = new Date(cursor.getLong(cursor.getColumnIndex("last_resolved_at")));

        try {
            String appLinkJSONArrayString = cursor.getString(cursor.getColumnIndex("android_app_link_json"));
            JSONArray appKinkJSONArray = new JSONArray(appLinkJSONArrayString);
            metadata.androidAppLinks.clear();
            metadata.androidAppLinks.addAll(AndroidAppLink.fromJSONArray(appKinkJSONArray));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return metadata;
    }

    public boolean isValidData() {
        return (url != null);
    }

    private static String buildIconUrl(String iconUrl, String url) {
        if (! iconUrl.startsWith("http")) {
            Uri fullUri = Uri.parse(url);
            Uri.Builder builder = fullUri.buildUpon();
            builder.path(iconUrl);
            return builder.toString();
        } else {
            return iconUrl;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public boolean hasLocation() {
        return (latitude != null) && (longitude != null);
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void clearLocation() {
        this.latitude = null;
        this.longitude = null;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 最後にBeaconをスキャンした日時
     */
    public Date getLastScannedAt() {
        return lastScannedAt;
    }

    public void setLastScannedAt(Date lastScannedAt) {
        this.lastScannedAt = lastScannedAt;
    }

    /**
     * 最後にMetadataをResolverから更新した日時
     */
    public Date getLastResolvedAt() {
        return lastResolvedAt;
    }

    public void setLastResolvedAt(Date lastResolvedAt) {
        this.lastResolvedAt = lastResolvedAt;
    }

    /**
     * AndroidのApp Link
     * @return
     */
    public List<AndroidAppLink> getAndroidAppLinks() {
        return androidAppLinks;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(iconUrl);
        dest.writeValue(latitude);
        dest.writeValue(longitude);
        dest.writeSerializable(lastScannedAt);
        dest.writeSerializable(lastResolvedAt);
        dest.writeInt(androidAppLinks.size());
        for (AndroidAppLink appLink : androidAppLinks) {
            dest.writeSerializable(appLink);
        }
    }

    public static final Creator<UrlMetadata> CREATOR = new Parcelable.Creator<UrlMetadata>() {
        @Override
        public UrlMetadata createFromParcel(Parcel source) {
            UrlMetadata metadata = new UrlMetadata();
            metadata.url = source.readString();
            metadata.title = source.readString();
            metadata.description = source.readString();
            metadata.iconUrl = source.readString();
            metadata.latitude = (Double)source.readValue(Double.class.getClassLoader());
            metadata.longitude = (Double)source.readValue(Double.class.getClassLoader());
            metadata.lastScannedAt = (Date)source.readSerializable();
            metadata.lastResolvedAt = (Date)source.readSerializable();
            int androidAppLinkCount = source.readInt();
            for (int i = 0; i < androidAppLinkCount; i++) {
                AndroidAppLink appLink = (AndroidAppLink)source.readSerializable();
                metadata.androidAppLinks.add(appLink);
            }
            return metadata;
        }

        @Override
        public UrlMetadata[] newArray(int size) {
            return new UrlMetadata[size];
        }
    };

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("url", url);
        json.put("title", title);
        json.put("description", description);
        json.put("iconUrl", iconUrl);
        json.put("latitude", latitude != null ? latitude : JSONObject.NULL);
        json.put("longitude", longitude != null ? longitude : JSONObject.NULL);
        json.put("lastScannedAt", DateUtils.simpleFormat(lastScannedAt));
        json.put("lastResolvedAt", DateUtils.simpleFormat(lastResolvedAt));
        json.put("androidAppLinks", AndroidAppLink.toJSONArray(androidAppLinks));
        return json;
    }

    public static final Unjsonizer<UrlMetadata> unjsonizer = new Unjsonizer<UrlMetadata>() {
        @Override
        public UrlMetadata fromJSONObject(JSONObject json) throws JSONException {
            UrlMetadata metadata = new UrlMetadata();
            metadata.url = json.getString("url");
            metadata.title = json.getString("title");
            metadata.description = json.getString("description");
            metadata.iconUrl = json.getString("iconUrl");
            if ((! json.isNull("latitude")) && (! json.isNull("longitude"))) {
                metadata.latitude = json.optDouble("latitude");
                metadata.longitude = json.optDouble("longitude");
            }
            try {
                metadata.lastScannedAt = DateUtils.parseSimpleFormat(json.getString("lastScannedAt"));
                metadata.lastResolvedAt = DateUtils.parseSimpleFormat(json.getString("lastResolvedAt"));
            } catch (ParseException e) {
                // ここには来ないはず
                throw new IllegalStateException();
            }
            metadata.androidAppLinks.addAll(AndroidAppLink.fromJSONArray(json.getJSONArray("androidAppLinks")));
            return metadata;
        }
    };
}
