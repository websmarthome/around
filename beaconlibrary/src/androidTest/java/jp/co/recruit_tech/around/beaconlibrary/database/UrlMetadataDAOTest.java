package jp.co.recruit_tech.around.beaconlibrary.database;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import jp.co.recruit_tech.around.beaconlibrary.database.dao.Transaction;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.UrlMetadataDAO;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;
import jp.co.recruit_tech.around.beaconlibrary.utils.DBUtils;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/09.
 */
public class UrlMetadataDAOTest extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        DBUtils.deleteDatabase(getContext());
    }

    public void testReadWrite() {
        Transaction<Void> trans = new Transaction<Void>();
        trans.runForTest(getContext(), new Transaction.Runner<Void>() {
            @Override
            public Void run(SQLiteDatabase db) {
                UrlMetadataDAO dao = new UrlMetadataDAO(db);
                UrlMetadata metadata;
                JSONObject json;
                Date now = new Date();

                metadata = dao.findMetadataFromUrl("http://example.com/page-1");
                assertNull(metadata);

                json = new JSONObject();
                try {
                    json.put("url", "http://example.com/page-1");
                    json.put("title", "page-1");
                    json.put("description", "desc-1");
                    json.put("icon", "http://example.com/icon-1.png");
                    metadata = new UrlMetadata();
                    metadata.setLastScannedAt(now);
                    metadata.setLastResolvedAt(now);
                    metadata.updateFromResolverJSON(json);
                    dao.upsert(metadata);

                    json.put("url", "http://example.com/page-2");
                    json.put("title", "page-2");
                    json.put("description", "desc-2");
                    json.put("icon", "http://example.com/icon-2.png");
                    metadata = new UrlMetadata();
                    metadata.setLastScannedAt(now);
                    metadata.setLastResolvedAt(now);
                    metadata.updateFromResolverJSON(json);
                    dao.upsert(metadata);

                    json.put("url", "http://example.com/page-3");
                    json.put("title", "page-3");
                    json.put("description", "desc-3");
                    json.put("icon", "http://example.com/icon-3.png");
                    metadata = new UrlMetadata();
                    metadata.setLastScannedAt(now);
                    metadata.setLastResolvedAt(now);
                    metadata.updateFromResolverJSON(json);
                    dao.upsert(metadata);

                    metadata = dao.findMetadataFromUrl("http://example.com/page-1");
                    assertEquals("http://example.com/page-1", metadata.getUrl());
                    assertEquals("page-1", metadata.getTitle());
                    assertEquals("desc-1", metadata.getDescription());
                    assertEquals("http://example.com/icon-1.png", metadata.getIconUrl());

                    metadata = dao.findMetadataFromUrl("http://example.com/page-3");
                    assertEquals("http://example.com/page-3", metadata.getUrl());
                    assertEquals("page-3", metadata.getTitle());
                    assertEquals("desc-3", metadata.getDescription());
                    assertEquals("http://example.com/icon-3.png", metadata.getIconUrl());
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                return null;
            }
        });
    }
}
