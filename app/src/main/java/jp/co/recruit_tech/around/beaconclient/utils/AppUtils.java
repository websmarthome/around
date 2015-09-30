package jp.co.recruit_tech.around.beaconclient.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Created by Hideaki on 15/03/18.
 */
public class AppUtils {
    /**
     * 外部アプリを開くIntentを得る
     */
    public static Intent getIntentToOpenExternalApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent intent = manager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return null;
        }
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return intent;
    }

    /**
     * 外部アプリを開く
     */
    public static boolean openExternalApp(Context context, String packageName) {
        Intent intent = getIntentToOpenExternalApp(context, packageName);
        if (intent != null) {
            context.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 外部アプリを開くことができるならtrue
     */
    public static boolean canOpenExternalApp(Context context, String packageName) {
        return getIntentToOpenExternalApp(context, packageName) != null;
    }

    /**
     * 指定アプリのGooglePlayStoreを開く
     */
    public static void openGooglePlayStore(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        context.startActivity(intent);
    }
}
