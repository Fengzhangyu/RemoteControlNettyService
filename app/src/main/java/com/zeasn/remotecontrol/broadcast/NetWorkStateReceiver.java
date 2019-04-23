package com.zeasn.remotecontrol.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.zeasn.remotecontrol.nsdhelper.NsdHelper;
import com.zeasn.remotecontrol.nsdhelper.NsdListener;
import com.zeasn.remotecontrol.nsdhelper.NsdService;
import com.zeasn.remotecontrol.nsdhelper.NsdType;
import com.zeasn.remotecontrol.service.RemoteControlService;
import com.zeasn.remotecontrol.service.netty.NettyHelper;
import com.zeasn.remotecontrol.utils.Const;
import com.zeasn.remotecontrol.utils.MLog;

import java.beans.PropertyChangeListener;

import static com.zeasn.remotecontrol.CustomApplication.isNetWorkConnect;

/**
 * 监听网络变化
 */

public class NetWorkStateReceiver extends BroadcastReceiver implements NsdListener {

    public static final String TAG = NetWorkStateReceiver.class.getSimpleName();

    Context mContext;

    private NsdHelper nsdHelper;


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();
        MLog.v(TAG, "onReceive" + action);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (nsdHelper == null) {
                nsdHelper = new NsdHelper(context, this);
            }
            if (networkInfo != null && networkInfo.isAvailable()) {
                if (!isNetWorkConnect) {
                    isNetWorkConnect = true;
                    /**无网到有网处理**/
                    MLog.v(TAG, "无网到有网处理");
//                    Toast.makeText(context, "无网到有网处理", Toast.LENGTH_SHORT).show();
                }
                String type = networkInfo.getTypeName();
                if (type.equalsIgnoreCase("ETHERNET") || type.equalsIgnoreCase("ETH")) {
                    if (isFastDoubleClick()) {
                        return;
                    }
                    MLog.v(TAG, "连接到以太网");
//                    Toast.makeText(context, "连接到以太网", Toast.LENGTH_SHORT).show();
                    initNetty(context);
//                    isNetConnect = true;
//                    if(isNetConnect) {
//
//                    }
                } else if (type.equalsIgnoreCase("WIFI")) {
                    if (isFastDoubleClick()) {
                        return;
                    }
                    MLog.v(TAG, "连接到WIFI");
//                    Toast.makeText(context, "连接到WIFI", Toast.LENGTH_SHORT).show();
                    initNetty(context);
                }
            } else {
                isNetWorkConnect = false;
                if (isFastDoubleClick()) {
                    return;
                }
                nsdHelper.stopDiscovery();
                nsdHelper.unregisterService();
                NettyHelper.getInstance().disconnect();
                MLog.v(TAG, "无网络");
//                Toast.makeText(context, "无网络", Toast.LENGTH_SHORT).show();
            }
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null) {
                String type = networkInfo.getTypeName();
                if (type.equalsIgnoreCase("WIFI")) {
                    if (isFastDoubleClick()) {
                        return;
                    }
                    MLog.v(TAG, "WIFI连接");
                }
            }
        }
    }

    private long lastClickTime;

    public boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 300) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    /**
     * 开启Socket 服务需要开启一个线程处理，等待客户端连接
     */
    private void initNetty(Context context) {
        nsdHelper.stopDiscovery();
        nsdHelper.unregisterService();
        Intent intent = new Intent();//context, RemoteControlService.class
        intent.setAction(Const.START_REMOTE_CONTROL_ACTION);
        intent.setPackage(context.getPackageName());
        context.startService(intent);

        nsdHelper.setLogEnabled(true);
        nsdHelper.registerService(RemoteControlService.NSD_SERVER_NAME, NsdType.HTTP);
        nsdHelper.startDiscovery(NsdType.HTTP);

    }

    @Override
    public void onNsdRegistered(NsdService registeredService) {

    }

    @Override
    public void onNsdDiscoveryFinished() {

    }

    @Override
    public void onNsdServiceFound(NsdService foundService) {

    }

    @Override
    public void onNsdServiceResolved(NsdService resolvedService) {

    }

    @Override
    public void onNsdServiceLost(NsdService lostService) {

    }

    @Override
    public void onNsdError(String errorMessage, int errorCode, String errorSource) {

    }
}
