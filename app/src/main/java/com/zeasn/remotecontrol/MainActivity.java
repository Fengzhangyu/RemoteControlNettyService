package com.zeasn.remotecontrol;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zeasn.remotecontrol.broadcast.NetWorkStateReceiver;
import com.zeasn.remotecontrol.event.EventSendController;
import com.zeasn.remotecontrol.utils.Const;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;


public class MainActivity extends AppCompatActivity {

    NetWorkStateReceiver mNetworkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent = new Intent();//context, RemoteControlService.class
        intent.setAction(Const.START_REMOTE_CONTROL_ACTION);
        intent.setPackage(getPackageName());
// 启动服务的地方
//        if (Build.VERSION.SDK_INT >= 26) {
//            context.startForegroundService(intent);
//        } else {
        // Pre-O behavior.
        startService(intent);

        registerNetWorkReceiver();


    }

    public void registerNetWorkReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mNetworkStateReceiver = new NetWorkStateReceiver();
        registerReceiver(mNetworkStateReceiver, filter);
    }

    /**
     * 更改遥控方式
     *
     * @param context
     * @param model
     */
    public static void updateInjectService(Context context, int model) {
        Intent intent = new Intent();
        intent.putExtra(EventSendController.KEY_INJECT_MODEL, model);
        intent.setAction(Const.UPDATE_INJECT_MODEL_ACTION);

        context.startService(intent);
    }


    /**
     * 获取Ip地址
     *
     * @return
     */

    public static InetAddress getWifiInetAddress() {
        InetAddress inetAddress = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return inetAddress;


    }
}
