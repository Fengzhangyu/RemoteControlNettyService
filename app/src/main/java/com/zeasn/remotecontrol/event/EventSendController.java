package com.zeasn.remotecontrol.event;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import cn.vtion.remotecontrol.tvserver.vstoresubclient.utils.KeyValue;
import cn.vtion.remotecontrol.tvserver.vstoresubclient.utils.MLog;
import cn.vtion.remotecontrol.tvserver.vstoresubclient.utils.MethodUtils;
import cn.vtion.remotecontrol.tvserver.vstoresubclient.utils.WindowUtils;

/**
 * 事件发送控制类
 */
public class EventSendController {

    /**
     * adb事件发送按键模式
     */
    public static final int ADB_MODEL = 0x0;
    /**
     * 调用外部插件发送按键模式 (打上原生系统签名)
     */
    public static final int PLUGIN_MODEL = 0x1;
    /**
     * JNI方式调用驱动事件模式
     */
    public static final int JNI_MODEL = 0x2;
    /**
     * 原生系统签名模式调用
     */
    public static final int PRIMARY_MODEL = 0x3;
    /**
     * 模拟按键调用模式 (默认为adb事件发送模式)
     */
    public int INJECT_MODEL = PLUGIN_MODEL;//如果方式改变要更改事件发送模式 插件模式

    public static final String KEY_INJECT_MODEL = "INJECT_MODEL";

    public Context mContext;
    public BaseEvent sendEvent;
    public AudioManager audio;

    public EventSendController(Context cxt) {
        mContext = cxt;

        initEventModel();
        initData();
    }

    /**
     * 初始化选择按键模式
     */
    public void initEventModel() {
        //判断签名是否为系统签名
        boolean priFlag = MethodUtils.isPrimarySigAvailable(mContext);
        if (priFlag) {
            INJECT_MODEL = PRIMARY_MODEL;
            return;
        }
        //判断是否已安装插件
//		boolean isInstalled = StorageUtil.isPkgInstalled(mContext,
//				StaticInstallReceiver.PLUGIN_PKG);
//		if (isInstalled) {
//			INJECT_MODEL = PLUGIN_MODEL;
//			return;
//		}
        //判断是否可以使用插件
//		boolean flag = MethodUtils.isPluginAvailable(mContext);
//		if (flag) {
//			showPlugin();
//		}

        // else {
        // Toast.makeText(mContext, "无法安装插件", Toast.LENGTH_LONG).show();
        // }

    }

    /**
     * 初始化数据
     */
    private void initData() {
        audio = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        if (INJECT_MODEL == ADB_MODEL) {
            sendEvent = new AdbEvent();
        } else if (INJECT_MODEL == PLUGIN_MODEL) {
            sendEvent = new RemotePlugin(mContext);
        } else if (INJECT_MODEL == JNI_MODEL) {
            sendEvent = new EventInjector();
        } else if (INJECT_MODEL == PRIMARY_MODEL) {
            sendEvent = new PrimarySigEvent();
        }
    }

    /**
     * 发送操控事件
     *
     * @param value
     */
    public void sendEvent(String value) {
        // 调节音量用系统方法
        if (Integer.parseInt(value) == KeyValue.KEYCODE_VOLUME_UP) {
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND
                            | AudioManager.FLAG_SHOW_UI);
        } else if (Integer.parseInt(value) == KeyValue.KEYCODE_VOLUME_DOWN) {
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND
                            | AudioManager.FLAG_SHOW_UI);
        } else {
            sendEvent.sendKeyEvent(Integer.parseInt(value));
        }

    }

    /**
     * 发送接收到的文本
     *
     * @param text
     */
    public void sendInputText(String text) {

//        MLog.e("text " + text);
//        Intent intent = new Intent(mContext, InputService.class);
//        intent.putExtra("msg", text);
//        mContext.startService(intent);
    }

    /**
     * createIntent
     *
     * @param context context
     * @param videoId youtube videoId
     * @return intent
     */
    public static Intent createIntent(Context context, String msg) {

        Intent _intent = new Intent("cn.zeasn.general.services.SHOW_AD");

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(_intent, 0);

        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        Intent explicitIntent = new Intent(_intent);
        explicitIntent.putExtra("msg", msg);
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    public static void send(String message) {
        int server_port = 8080;
        DatagramSocket s = null;
        try {
            s = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress local = null;
        try {
            local = InetAddress.getByName("172.16.0.58");
            MLog.e("text local " + local);
            s.connect(local, server_port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int msg_length = message.length();
        byte[] messageByte = message.getBytes();
        DatagramPacket p = new DatagramPacket(messageByte, msg_length, local,
                server_port);
        try {
            s.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设定操控模式
     *
     * @param model
     */
    public void setInjectModel(int model) {
        INJECT_MODEL = model;

        initData();
    }

    /**
     * 显示安装插件弹窗
     */
    public void showPlugin() {
        WindowUtils.showPopupWindow(mContext);
        /*
         * Builder builder = new AlertDialog.Builder(mContext);
         * builder.setTitle("Vtion遥控小助手");
         * builder.setMessage("为了遥控功能更好用，强烈建议您安装Vtion遥控小助手");
         * builder.setPositiveButton("安装", new OnClickListener() {
         *
         * @Override public void onClick(DialogInterface dialog, int which) {
         * Toast.makeText(mContext, "执行安装插件操作", Toast.LENGTH_LONG).show();
         * dialog.cancel(); } }); builder.setPositiveButton("取消", new
         * OnClickListener() {
         *
         * @Override public void onClick(DialogInterface dialog, int which) {
         * dialog.cancel(); } }); AlertDialog dialog = builder.create();
         * dialog.show();
         */
    }

}
