package jp.co.recruit_tech.around.beaconlibrary.metadata;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jp.co.recruit_tech.around.beaconlibrary.utils.JSONUtils;
import jp.co.recruit_tech.around.beaconlibrary.utils.Jsonizable;
import jp.co.recruit_tech.around.beaconlibrary.utils.Unjsonizer;

/**
 * Metadataを重複なく管理する。
 *
 * Created by Hideaki on 15/01/30.
 */
public class UrlMetadataSet implements Jsonizable, Parcelable {
    private List<UrlMetadata> metadataList;

    public UrlMetadataSet() {
        metadataList = new ArrayList<UrlMetadata>();
    }
    public UrlMetadataSet(List<UrlMetadata> metadataList) {
        this.metadataList = metadataList;
    }

    public void upsert(UrlMetadata metadata) {
        int index = indexOfUrl(metadata.getUrl());
        if (index >= 0) {
            metadataList.remove(index);
            metadataList.add(index, metadata);
        } else {
            metadataList.add(metadata);
        }
    }

    public void sortByScannedAt() {
        Collections.sort(metadataList, new Comparator<UrlMetadata>() {
            @Override
            public int compare(UrlMetadata urlMetadata, UrlMetadata urlMetadata2) {
                long sub = urlMetadata.getLastScannedAt().getTime() - urlMetadata2.getLastScannedAt().getTime();
                if (sub < 0) {
                    return 1;
                } else if (sub > 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

    public int indexOfUrl(String url) {
        for (int i = 0; i < metadataList.size(); i++) {
            UrlMetadata metadata = metadataList.get(i);
            if (metadata.getUrl().equals(url)) {
                return i;
            }
        }
        return -1;
    }

    public UrlMetadataSet query(String query) {
        query = query.toLowerCase();
        ArrayList<UrlMetadata> matchedList = new ArrayList<UrlMetadata>();
        for (UrlMetadata metadata : metadataList) {
            if (metadata.getTitle().toLowerCase().indexOf(query) >= 0) {
                matchedList.add(metadata);
            }
        }
        return new UrlMetadataSet(matchedList);
    }

    public int size() {
        return metadataList.size();
    }

    public UrlMetadata get(int i) {
        return metadataList.get(i);
    }

    public void clear() {
        metadataList.clear();
    }

    /**
     * 期限切れのものを削除する
     */
    public int deleteExpired(long periodMillis) {
        long now = new Date().getTime();
        int count = 0;
        for (int i = metadataList.size() - 1; i >= 0; i--) {
            UrlMetadata metadata = metadataList.get(i);
            if (now - metadata.getLastScannedAt().getTime() > periodMillis) {
                metadataList.remove(i);
                count++;
            }
        }
        return count;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("metadataList", JSONUtils.toJSONArray(metadataList));
        return json;
    }

    public static final Unjsonizer<UrlMetadataSet> unjsonizer = new Unjsonizer<UrlMetadataSet>() {
        @Override
        public UrlMetadataSet fromJSONObject(JSONObject json) throws JSONException {
            List<UrlMetadata> metadataList = JSONUtils.fromJSONArray(json.getJSONArray("metadataList"), UrlMetadata.unjsonizer);
            return new UrlMetadataSet(metadataList);
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try {
            dest.writeString(toJSONObject().toString());
        } catch (JSONException e) {
            // ここには来ないはず
            throw new IllegalStateException();
        }
    }

    public static final Creator<UrlMetadataSet> CREATOR = new Parcelable.Creator<UrlMetadataSet>() {
        @Override
        public UrlMetadataSet createFromParcel(Parcel source) {
            try {
                return unjsonizer.fromJSONObject(new JSONObject(source.readString()));
            } catch (JSONException e) {
                // ここには来ないはず
                throw new IllegalStateException();
            }
        }
        @Override
        public UrlMetadataSet[] newArray(int size) {
            return new UrlMetadataSet[size];
        }
    };
}
