package jp.co.recruit_tech.around.beaconlibrary.service;

import android.content.Context;
import android.content.Intent;

/**
 * サービスが外部から受け取るコマンド。
 * 非同期処理、かつ、サービスから結果を要求しないもののみコマンドにする。
 * 同期、かつ結果要求するものはServiceInterfaceで実装する。
 *
 * Created by Hideaki on 15/03/13.
 */
public class ServiceCommand {
    public static final String ACTION_KEY = "action";

    /**
     * Beaconスキャンを開始する。
     * Activity側でBluetoothが有効になったタイミングで送信すること
     */
    public static class StartBleScan {
        public static String getActionName(){
            return ServiceCommand.class.getCanonicalName() + ".START_BLE_SCAN";
        }

        /**
         * コマンド送信
         */
        public static void send(Context context) {
            Intent intent = new Intent(context, BeaconLibraryMainService.class);
            intent.putExtra(ACTION_KEY, getActionName());
            context.startService(intent);
        }
    }

    /**
     * スキャンされたBeaconキャッシュをクリアする
     */
    public static class ClearScannedBeaconCache {
        public static String getActionName(){
            return ServiceCommand.class.getCanonicalName() + ".CLEAR_BEACON_CACHE";
        }

        /**
         * コマンド送信
         */
        public static void send(Context context) {
            Intent intent = new Intent(context, BeaconLibraryMainService.class);
            intent.putExtra(ACTION_KEY, getActionName());
            context.startService(intent);
        }
    }

    /**
     * Metadata Resolverを再起動する。
     */
    public static class RestartMetadataResolver {
        public static String getActionName(){
            return ServiceCommand.class.getCanonicalName() + ".RESTART_METADATA_RESOLVER";
        }

        /**
         * コマンド送信
         */
        public static void send(Context context) {
            Intent intent = new Intent(context, BeaconLibraryMainService.class);
            intent.putExtra(ACTION_KEY, getActionName());
            context.startService(intent);
        }
    }

    public static class ReloadServiceSetting {
        /**
         * コマンド送信
         */
        public static void send(Context context) {
            ClearScannedBeaconCache.send(context);
            StartBleScan.send(context);
            RestartMetadataResolver.send(context);
        }
    }
}
