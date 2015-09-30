package jp.co.recruit_tech.around.beaconlibrary.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.recruit_tech.around.beaconlibrary.metadata.AndroidAppLink;
import jp.co.recruit_tech.around.beaconlibrary.metadata.UrlMetadata;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/08.
 */
public class UrlMetadataDAO {
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "url_metadatas";

    private SQLiteDatabase db;

    public static void createTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME + " (" +
                        "id                INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "url               TEXT UNIQUE NOT NULL," +
                        "title             TEXT," +
                        "description       TEXT," +
                        "icon_url          TEXT, " +
                        "latitude          REAL," +
                        "longitude         REAL," +
                        "last_scanned_at   REAL NOT NULL," +
                        "last_resolved_at  REAL NOT NULL," +
                        /* AppLinkはJSON Stringで保存する */
                        "android_app_link_json      TEXT," +
                        "created_at        REAL NOT NULL," +
                        "updated_at        REAL NOT NULL" +
                        ");"
        );
    }

    public UrlMetadataDAO(SQLiteDatabase db) {
        this.db = db;
    }

    public void upsert(UrlMetadata metadata) {
        UrlMetadata cachedMetadata = findMetadataFromUrl(metadata.getUrl());
        Date now = new Date();

        ContentValues values = new ContentValues();
        values.put("url", metadata.getUrl());
        values.put("title", metadata.getTitle());
        values.put("description", metadata.getDescription());
        values.put("icon_url", metadata.getIconUrl());
        values.put("latitude", metadata.getLatitude());
        values.put("longitude", metadata.getLongitude());
        values.put("last_scanned_at", metadata.getLastScannedAt().getTime());
        values.put("last_resolved_at", metadata.getLastResolvedAt().getTime());
        values.put("last_resolved_at", metadata.getLastResolvedAt().getTime());
        try {
            String jsonString = AndroidAppLink.toJSONArray(metadata.getAndroidAppLinks()).toString();
            values.put("android_app_link_json", jsonString);
        } catch (JSONException ex) {
            // ここにはこないはず
            ex.printStackTrace();
        }
        values.put("updated_at", now.getTime());

        if (cachedMetadata != null) {
            db.update(TABLE_NAME, values, "url = ?", new String[] { metadata.getUrl() });
        } else {
            values.put("created_at", now.getTime());
            db.insert(TABLE_NAME, null, values);
        }
    }

    public List<UrlMetadata> findAll() {
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        List<UrlMetadata> metadataList = new ArrayList<UrlMetadata>();
        try {
            while (cursor.moveToNext()) {
                UrlMetadata metadata = metadataFromCursor(cursor);
                metadataList.add(metadata);
            }
        } finally {
            cursor.close();
        }
        return metadataList;
    }

    public UrlMetadata findMetadataFromUrl(final String url) {
        Cursor cursor = db.query(TABLE_NAME, null, "url = ?", new String[]{ url }, null, null, null);
        UrlMetadata metadata = null;
        try {
            if (cursor.moveToNext()) {
                metadata = metadataFromCursor(cursor);
            }
        } finally {
            cursor.close();
        }
        return metadata;
    }

    public UrlMetadata findMetadataFromUrl(final String url, long availableMillis) {
        UrlMetadata metadata = findMetadataFromUrl(url);
        if (metadata == null) return null;

        Date now = new Date();
        if (now.getTime() - metadata.getLastResolvedAt().getTime() <= availableMillis) {
            return metadata;
        }

        return null;
    }

    public List<UrlMetadata> findMetadatasInRange(double top, double left, double bottom, double right, int limit) {
        ArrayList<UrlMetadata> metadatas = new ArrayList<UrlMetadata>();
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                "latitude >= ? AND latitude <= ?" +
                " AND longitude >= ? AND longitude <= ?",
                new String[]{ String.valueOf(top), String.valueOf(bottom), String.valueOf(left), String.valueOf(right) },
                null, null, null, String.valueOf(limit));
        try {
            while (cursor.moveToNext()){
                UrlMetadata metadata = metadataFromCursor(cursor);
                metadatas.add(metadata);
            }
        } finally {
            cursor.close();
        }
        return metadatas;
    }

    public void deleteAll() {
        db.delete(TABLE_NAME, null, null);
    }

    private UrlMetadata metadataFromCursor(Cursor cursor) {
        return UrlMetadata.fromDBCursor(cursor);
    }
}
