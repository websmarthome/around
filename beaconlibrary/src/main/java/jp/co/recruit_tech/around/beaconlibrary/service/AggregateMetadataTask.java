package jp.co.recruit_tech.around.beaconlibrary.service;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import jp.co.recruit_tech.around.beaconlibrary.Consts;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadataSet;

/**
 * 直近のMetadataを集約管理するタスク。
 *
 * Created by kusakabe on 15/02/21.
 */
public class AggregateMetadataTask extends Task {
    public interface Listener {
        public void onAggregate(UrlMetadataSet metadataSet);
    }

    private static final String PREFERENCES_NAME = "jp.co.recruit_tech.around.beaconlibrary.service.AggregateMetadataTask.PreferencesName";
    private static final String PREFERENCES_KEY_URL_METADATA_SET = "urlMetadataSet";

    /** 期限切れ確認間隔 */
    private static final int CHECK_EXPIRE_PERIOD = 1 * 60 * 1000;

    private Context context;
    private SharedPreferences sharedPreferences;
    private UrlMetadataSet urlMetadataSet;
    private Timer timer;
    private Listener listener;

    public AggregateMetadataTask(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onStart() {
        load();
        IntentFilter intentFilter = new IntentFilter();
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                deleteExpiredMetadata();
            }
        }, CHECK_EXPIRE_PERIOD, CHECK_EXPIRE_PERIOD);
    }

    @Override
    protected void onStop() {
        timer.cancel();
        timer = null;
        urlMetadataSet = null;
    }

    private void save() {
        try {
            sharedPreferences
            .edit()
            .putString(PREFERENCES_KEY_URL_METADATA_SET, urlMetadataSet.toJSONObject().toString())
            .commit();
        } catch (JSONException e) {
            // ここには来ないはず
            throw new IllegalStateException();
        }
    }

    private void load() {
        urlMetadataSet = null;
        if (sharedPreferences.contains(PREFERENCES_KEY_URL_METADATA_SET)) {
            String urlMatadataSetString = sharedPreferences.getString(PREFERENCES_KEY_URL_METADATA_SET, "{}");
            try {
                urlMetadataSet = UrlMetadataSet.unjsonizer.fromJSONObject(new JSONObject(urlMatadataSetString));
            } catch (JSONException e) {
                // データのフォーマとが変わった時など失敗する
                // その場合、そのデータは捨てる
            }
        }
        if (urlMetadataSet == null) {
            urlMetadataSet = new UrlMetadataSet();
        }
        deleteExpiredMetadata();
        if (listener != null) {
            listener.onAggregate(urlMetadataSet);
        }
    }

    /**
     * 直近のMetadataとして期限切れのものを削除
     * DB上から削除するわけではないことに注意
     */
    private void deleteExpiredMetadata() {
        int deletedCount = urlMetadataSet.deleteExpired(Consts.AGGREGATE_METADATA_PERIOD);
        if (deletedCount > 0) {
            save();
        }
    }

    /**
     * Metadataを追加・更新する
     */
    public void upsertMetadata(UrlMetadata urlMetadata) {
        // 期限切れのものは削除
        deleteExpiredMetadata();
        // 新しく得られたMetadataを保存
        urlMetadataSet.upsert(urlMetadata);
        save();
        if (listener != null) {
            listener.onAggregate(urlMetadataSet);
        }
    }

    public void clearMetadataSet() {
        urlMetadataSet.clear();
        save();
    }

    public UrlMetadataSet getCurrentUrlMetadataSet() {
        return urlMetadataSet;
    }
}
