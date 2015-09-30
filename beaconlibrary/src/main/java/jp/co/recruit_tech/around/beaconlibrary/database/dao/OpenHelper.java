package jp.co.recruit_tech.around.beaconlibrary.database.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/08.
 */
public class OpenHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;

    public OpenHelper(Context ctx, String filename) {
        super(ctx, filename, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ShortUrlDAO.createTable(db);
        UrlMetadataDAO.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
