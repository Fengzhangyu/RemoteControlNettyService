package com.zeasn.remotecontrol.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.media.AudioManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zeasn.remotecontrol.MainActivity;
import com.zeasn.remotecontrol.event.EventSendController;
import com.zeasn.remotecontrol.utils.MLog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Created by Devin.F on 2019/1/15.
 */
public class RemoteControlService extends Service implements PropertyChangeListener {

    private static final String TAG = "RemoteControlService";

    public AudioManager audio = null;

    public EventSendController eventSendController;

    /**
     * 注册 NSD 服务的名称 和 端口 这个可以设置默认固定址，用于客户端通过 NSD_SERVER_NAME 筛选得到服务端地址和端口
     */
    public static String NSD_SERVER_NAME = "Whale Tv - Netty";

    public static final int PORT = 5051;

    public static boolean isConnected = false;    //记录tv是否有设备连接

    public Watcher watcher;

    @Override
    public void onCreate() {
        MLog.d("onStartCommand，onCreate");
        init();
        /** 将service变为前台服务，防止被轻易杀掉(360,猎豹有效)*/
        startForeground(1, new Notification());
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }

    /**
     * 初始化操作
     */
    public void init() {
        eventSendController = new EventSendController(getApplicationContext());

        audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

        watcher = new Watcher(this);
        String uid = "";
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ApplicationInfo appinfo = getApplicationInfo();
        List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
        for (RunningAppProcessInfo runningProcess : run) {
            if ((runningProcess.processName != null)
                    && runningProcess.processName.equals(appinfo.processName)) {
                uid = String.valueOf(runningProcess.uid);
                break;
            }
        }
        MLog.e("UID>>>>   " + uid);
        watcher.createAppMonitor(uid);
    }


    /**
     * 服务器端注册一个可供NSD探测到的网络 Ip 地址，便于给展示叫号机连接此socket
     */
    Runnable nsdServerRunnable = new Runnable() {
        @Override
        public void run() {

            NSDServer nsdServer = new NSDServer();
            nsdServer.startNSDServer(RemoteControlService.this, NSD_SERVER_NAME, PORT);

            nsdServer.setRegisterState(new NSDServer.IRegisterState() {
                @Override
                public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                    Log.i(TAG, "已注册服务onServiceRegistered: " + serviceInfo.toString());
                    //已经注册可停止该服务
//                    nsdServer.stopNSDServer();
                }

                @Override
                public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

                }

                @Override
                public void onServiceUnregistered(NsdServiceInfo serviceInfo) {

                }

                @Override
                public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

                }
            });
        }
    };

    private void registerNsdServer() {

        new Thread(nsdServerRunnable).start();
    }

}
