package jp.co.recruit_tech.around.beaconlibrary.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import jp.co.recruit_tech.around.beaconlibrary.Consts;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.ShortUrlDAO;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.Transaction;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.UrlMetadataDAO;
import jp.co.recruit_tech.around.beaconlibrary.metadataserver.MetadataResolver;
import jp.co.recruit_tech.around.beaconlibrary.metadataserver.MetadataResolverFactory;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.scanner.UriBeaconData;

/**
 * Created by kusakabe on 15/02/21
 * BeaconDataを受け取るとResolveServerに問い合わせUrlMetadataを得る。
 * 得たUrlMetadataはListenerを通じて渡す。
 * mock設定を切り替えた場合はcreateRestartIntentをstartServiceすれば切り替わる。
 */
public class UrlMetadataTask extends Task {
    public interface Listener {
        /**
         * URLに対応するMetadataを得られたら呼ばれる
         */
        public void onResolveUrl(UrlMetadata metadata);
    }

    private Context context;
    private MetadataResolver metadataResolver;
    private Listener listener;

    public UrlMetadataTask(Context context) {
        this.context = context;
        restartMetadataResolver();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onStop() {
    }

    /**
     * MetadataResolver再起動
     */
    public void restartMetadataResolver() {
        metadataResolver = MetadataResolverFactory.getInstance().getMetadataResolver(context);
        metadataResolver.setMetadataResolverCallback(new MetadataResolver.RequestCallback() {
            @Override
            public void onUrlMetadataReceived(String requestUrl, UrlMetadata urlMetadata) {
                onMetadataReceive(requestUrl, urlMetadata);
            }

            @Override
            public void onUrlMetadataIconReceived(UrlMetadata urlMetadata) {
                onMetadataReceive(null, urlMetadata);
            }
        });
    }

    /**
     * BeaconDataの内容をResolveサーバーに渡してMetadataを得る。
     * 結果はListenerを通じて得られる。
     */
    public void resolveUrl(final UriBeaconData uriBeaconData) {
        //
        // キャッシュにMetadataがあれば読む
        //
        Transaction<UrlMetadata> transaction = new Transaction<UrlMetadata>();
        UrlMetadata urlMetadataCache = transaction.run(context, new Transaction.Runner<UrlMetadata>() {
            @Override
            public UrlMetadata run(SQLiteDatabase db) {
                ShortUrlDAO shortUrlDAO = new ShortUrlDAO(db);
                UrlMetadataDAO urlMetadataDAO = new UrlMetadataDAO(db);
                String finalUrl = shortUrlDAO.finalUrlFromShortUrl(uriBeaconData.getUriString());
                if (finalUrl == null) {
                    finalUrl = uriBeaconData.getUriString();
                }
                UrlMetadata metadata = urlMetadataDAO.findMetadataFromUrl(finalUrl, Consts.METADATA_AVAILABLE_PERIOD);
                if (metadata != null) {
                    // スキャン日時を更新
                    metadata.setLastScannedAt(new Date());
                    urlMetadataDAO.upsert(metadata);
                }
                return metadata;
            }
        });
        if (urlMetadataCache == null) {
            // キャッシュがなければMetadataServerへ問い合わせ
            metadataResolver.requestUrlMetadata(uriBeaconData.getUriString());
            return;
        }

        if (listener != null) {
            listener.onResolveUrl(urlMetadataCache);
        }
    }

    private void onMetadataReceive(final String requestUrl, final UrlMetadata urlMetadata) {
        Transaction<Void> transaction = new Transaction<Void>();
        transaction.run(context, new Transaction.Runner<Void>() {
            @Override
            public Void run(SQLiteDatabase db) {
                if (requestUrl != null) {
                    ShortUrlDAO shortUrlDAO = new ShortUrlDAO(db);
                    shortUrlDAO.saveUrlMap(requestUrl, urlMetadata.getUrl());
                }
                UrlMetadataDAO urlMetadataDAO = new UrlMetadataDAO(db);
                urlMetadataDAO.upsert(urlMetadata);
                return null;
            }
        });
        if (listener != null) {
            listener.onResolveUrl(urlMetadata);
        }
    }
}
