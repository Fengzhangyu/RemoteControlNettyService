package com.zeasn.remotecontrol.event;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.vtion.tv.remotecontrolplugin.mInterface;
import com.zeasn.remotecontrol.utils.MLog;

/**
 * 模拟按键插件控制类
 */
public class RemotePlugin extends BaseEvent {

    private final String PLUGIN_ACTION = "com.vtion.tv.remotecontrolplugin";

    private Context mContext;
    private mInterface mService;
    private boolean flag = false;

    public RemotePlugin(Context cxt) {
        mContext = cxt;

        init();
    }

    private void init() {
        Intent intent = new Intent(PLUGIN_ACTION);
        intent.setPackage(mContext.getPackageName());
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MLog.d("RemotePlugin onServiceConnected...");

            mService = mInterface.Stub.asInterface(service);
            flag = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MLog.d("RemotePlugin onServiceDisconnected...");

            mService = null;
            flag = false;
        }

    };

    /**
     * 发送按键事件
     *
     * @param keyValue
     * @throws RemoteException
     */
    public void sendKeyEvent(int keyValue) {
        try {
            if (flag)
                mService.sendKeyEvent(keyValue);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送显示文本
     *
     * @param content
     */
    public void sendString(String content) {
        try {
            if (flag)
                mService.sendString(content);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
