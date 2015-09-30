package jp.co.recruit_tech.around.beaconlibrary.service;

import android.content.Intent;

/**
 * Created by Hideaki on 15/03/13.
 */
public class ServiceCommandReceiver {
    /**
     * Command Intentの受信インタフェース
     */
    public interface Listener {
        /**
         * BLEスキャン開始要求
         */
        public void onRequestStartBleScan();

        /**
         * スキャンされたBeaconキャッシュをクリア要求
         */
        public void onRequestClearScannedBeaconCache();

        /**
         * MetadataResolverを再起動
         */
        public void onRequestRestartMetadataResolver();
    }
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Service#onStartCommandが呼ばれた時に呼ぶ。
     * Listenerへふるい分けを行う
     */
    public void onStartCommand(Intent intent) {
        if (listener == null) return;

        String key = intent.getStringExtra(ServiceCommand.ACTION_KEY);
        if (key == null) return;

        if (key.equals(ServiceCommand.StartBleScan.getActionName())) {
            listener.onRequestStartBleScan();
        } else if (key.equals(ServiceCommand.ClearScannedBeaconCache.getActionName())) {
            listener.onRequestClearScannedBeaconCache();
        } else if (key.equals(ServiceCommand.RestartMetadataResolver.getActionName())) {
            listener.onRequestRestartMetadataResolver();
        }
    }
}
