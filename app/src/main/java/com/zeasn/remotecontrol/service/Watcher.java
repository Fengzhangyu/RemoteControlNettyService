package com.zeasn.remotecontrol.service;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.zeasn.remotecontrol.utils.Const;

import java.util.ArrayList;

/**
 * Created by Devin.F on 2019/1/15.
 * c端守护类，用于守护Service，在被系统杀死后能自动重启
 *
 */
public class Watcher {

    static {
        System.loadLibrary("RemoteControl");
    }

    //	public static final String PACKAGE = "cn.vtion.remotecontrol.tvserver.vstoresubclient/";
//	public String mMonitoredService = "cn.vtion.remotecontrol.tvserver.vstoresubclient.sercice.RemoteControlService";
    private volatile boolean bHeartBreak = false;
    private Context mContext;
    private boolean mRunning = true;

    public void createAppMonitor(String userId) {
        if (!createWatcher(userId)) {
            Log.e("Watcher", "<<Monitor created failed>>");
        } else {
            Log.e("Watcher", "<<Monitor created success>>");
        }
    }

    public Watcher(Context context) {
        mContext = context;
    }

//    public int isServiceRunning() {
//        ActivityManager am = (ActivityManager) mContext
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) am
//                .getRunningServices(1024);
//        for (int i = 0; i < runningService.size(); ++i) {
//            if (Const.mMonitoredService.equals(runningService.get(i).service
//                    .getClassName().toString())) {
//                return 1;
//            }
//        }
//        return 0;
//    }

    /**
     * Native方法，创建一个监视子进程.
     *
     * @param userId
     *            当前进程的用户ID,子进程重启当前进程时需要用到当前进程的用户ID.
     * @return 如果子进程创建成功返回true，否则返回false
     */
    public native boolean createWatcher(String userId);

    /**
     * Native方法，让当前进程连接到监视进程.
     *
     * @return 连接成功返回true，否则返回false
     */
//	private native boolean connectToMonitor();

    /**
     * Native方法，向监视进程发送任意信息
     *
     * @param 发给monitor的信息
     * @return 实际发送的字节
     */
    private native int sendMsgToMonitor(String msg);

}
