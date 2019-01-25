package com.zeasn.remotecontrol.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.zeasn.remotecontrol.broadcast.NetWorkStateReceiver;
import com.zeasn.remotecontrol.utils.Const;


/**
 * 开机启动广播
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    NetWorkStateReceiver mNetworkStateReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BOOT_COMPLETED.equals(action)) {

            Intent intentRemo = new Intent();//context, RemoteControlService.class
            intentRemo.setAction(Const.START_REMOTE_CONTROL_ACTION);
            intentRemo.setPackage(context.getPackageName());
// 启动服务的地方
//        if (Build.VERSION.SDK_INT >= 26) {
//            context.startForegroundService(intent);
//        } else {
            // Pre-O behavior.
            context.startService(intentRemo);

            registerNetWorkReceiver(context);
        }
    }

    public void registerNetWorkReceiver(Context context) {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mNetworkStateReceiver = new NetWorkStateReceiver();
        context.registerReceiver(mNetworkStateReceiver, filter);
    }

}
