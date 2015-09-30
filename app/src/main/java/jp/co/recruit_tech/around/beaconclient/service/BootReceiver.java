package jp.co.recruit_tech.around.beaconclient.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jp.co.recruit_tech.around.beaconlibrary.service.BeaconLibraryMainService;

/**
 * Created by kusakabe on 15/03/20.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, BeaconLibraryMainService.class));
        context.startService(new Intent(context, WearService.class));
    }
}
