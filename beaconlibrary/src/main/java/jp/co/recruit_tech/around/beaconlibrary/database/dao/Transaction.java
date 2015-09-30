package jp.co.recruit_tech.around.beaconlibrary.database.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import jp.co.recruit_tech.around.beaconlibrary.database.DBConsts;

/**
 * Created by MIYAMOTO, Hideaki on 15/02/08.
 */
public class Transaction<T> {
    public static interface Runner<T> {
        public T run(SQLiteDatabase db);
    }

    public T run(Context context, Runner<T> runner) {
        return run(context, DBConsts.APP_DATABASE_FILENAME, runner);
    }

    public T runForTest(Context context, Runner<T> runner) {
        return run(context, DBConsts.TEST_DATABASE_FILENAME, runner);
    }

    private T run(Context context, String filename, Runner<T> runner) {
        SQLiteOpenHelper helper = new OpenHelper(context, filename);
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                T ret = runner.run(db);
                db.setTransactionSuccessful();
                return ret;
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
    }
}
