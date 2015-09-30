package jp.co.recruit_tech.around.beaconlibrary.database;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.io.File;

import jp.co.recruit_tech.around.beaconlibrary.database.dao.Transaction;
import jp.co.recruit_tech.around.beaconlibrary.database.dao.ShortUrlDAO;
import jp.co.recruit_tech.around.beaconlibrary.utils.DBUtils;

/**
 * Created by Hideaki on 15/02/08.
 */
public class ShortUrlDAOTest extends AndroidTestCase {
    public ShortUrlDAOTest() {
    }

    @Override
    protected void setUp() throws Exception {
        DBUtils.deleteDatabase(getContext());
    }

    public void testSave() {
        Transaction<Void> trans = new Transaction<Void>();
        trans.runForTest(getContext(), new Transaction.Runner<Void>() {
            @Override
            public Void run(SQLiteDatabase db) {
                ShortUrlDAO store = new ShortUrlDAO(db);
                String finalUrl;

                finalUrl = store.finalUrlFromShortUrl("http://goo.gl/00001");
                assertNull(finalUrl);

                store.saveUrlMap("http://goo.gl/00001", "http://example.com/page-1");
                store.saveUrlMap("http://goo.gl/00002", "http://example.com/page-2");
                store.saveUrlMap("http://goo.gl/00003", "http://example.com/page-3");

                finalUrl = store.finalUrlFromShortUrl("http://goo.gl/00001");
                assertEquals("http://example.com/page-1", finalUrl);

                finalUrl = store.finalUrlFromShortUrl("http://goo.gl/00002");
                assertEquals("http://example.com/page-2", finalUrl);

                finalUrl = store.finalUrlFromShortUrl("http://goo.gl/00003");
                assertEquals("http://example.com/page-3", finalUrl);

                return null;
            }
        });
   }
}
