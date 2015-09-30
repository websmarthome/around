package jp.co.recruit_tech.around.beaconlibrary.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/08.
 */
public class ShortUrlDAO {
    private static final int DB_VERSION = 1;
    private static String TABLE_NAME = "short_urls";

    private SQLiteDatabase db;

    static void createTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME + " (" +
                        "id              INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "short_url       TEXT UNIQUE NOT NULL," +
                        "final_url       TEXT NOT NULL" +
                        ");"
        );
    }

    public ShortUrlDAO(SQLiteDatabase db) {
        this.db = db;
    }

    public void saveUrlMap(String shortUrl, String finalUrl) {
        String cachedFinalUrl = finalUrlFromShortUrl(shortUrl);
        if (cachedFinalUrl != null && cachedFinalUrl.equals(finalUrl)) {
            return;
        }

        if (cachedFinalUrl != null) {
            ContentValues values = new ContentValues();
            values.put("final_url", finalUrl);
            db.update(TABLE_NAME, values, "short_url = ?", new String[] { shortUrl });
        } else {
            ContentValues values = new ContentValues();
            values.put("short_url", shortUrl);
            values.put("final_url", finalUrl);
            db.insertOrThrow(TABLE_NAME, null, values);
        }
    }

    public String finalUrlFromShortUrl(final String shortUrl) {
        Cursor cursor = db.query(TABLE_NAME, null, "short_url = ?", new String[]{ shortUrl }, null, null, null);
        String finalUrl = null;
        try {
            if (cursor.moveToNext()) {
                finalUrl = cursor.getString(cursor.getColumnIndex("final_url"));
            }
        } finally {
            cursor.close();
        }
        return finalUrl;
    }

    public void deleteAll() {
        db.delete(TABLE_NAME, null, null);
    }
}
