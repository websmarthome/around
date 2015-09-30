package jp.co.recruit_tech.around.beaconlibrary.utils;

import android.content.Context;

import java.io.File;

import jp.co.recruit_tech.around.beaconlibrary.database.DBConsts;

/**
 * Created by Hideaki on 15/03/10.
 */
public class DBUtils {
    public static void deleteDatabase(Context context) {
        File file = new File(context.getCacheDir() + "/../databases/" + DBConsts.TEST_DATABASE_FILENAME);
        file.delete();
    }
}
