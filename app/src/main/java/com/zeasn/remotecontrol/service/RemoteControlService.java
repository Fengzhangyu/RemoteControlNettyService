package com.zeasn.remotecontrol.service;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.media.AudioManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zeasn.remotecontrol.CustomApplication;
import com.zeasn.remotecontrol.MainActivity;
import com.zeasn.remotecontrol.event.EventSendController;
import com.zeasn.remotecontrol.interfaces.NettyListener;
import com.zeasn.remotecontrol.service.netty.NSDServer;
import com.zeasn.remotecontrol.service.netty.NettyHelper;
import com.zeasn.remotecontrol.utils.Const;
import com.zeasn.remotecontrol.utils.ExPackageManager;
import com.zeasn.remotecontrol.utils.KeyUtil;
import com.zeasn.remotecontrol.utils.KeyValue;
import com.zeasn.remotecontrol.utils.LocalAppManager;
import com.zeasn.remotecontrol.utils.MLog;
import com.zeasn.remotecontrol.utils.TlvBox;
import com.zeasn.remotecontrol.utils.WindowUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by Devin.F on 2019/1/15.
 */
public class RemoteControlService extends Service implements PropertyChangeListener {

    private static final String TAG = "RemoteControlService";
    private static final String BROADCAST_ACTION = "com.zeasn.remotecontrol";

    public AudioManager audio = null;

    public EventSendController eventSendController;

    /**
     * 注册 NSD 服务的名称 和 端口 这个可以设置默认固定址，用于客户端通过 NSD_SERVER_NAME 筛选得到服务端地址和端口
     */
    public static String NSD_SERVER_NAME = "Whale Tv - " + WindowUtils.getMacDefault(CustomApplication.getContext());

    public static final int PORT = 105051;
//    public static final int PORT = 5051;

    public static boolean isConnected = false;    //记录tv是否有设备连接

    public Watcher watcher;

    private Handler handler;

    @Override
    public void onCreate() {

        MLog.d("onStartCommand，onCreate");
        handler = new Handler(Looper.getMainLooper());
        init();
        /** 将service变为前台服务，防止被轻易杀掉(360,猎豹有效)*/
        startForeground(1, new Notification());
        registerNsdServer();
        initNetty();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MLog.d("onStartCommand，intent对象不为空");
        flags = START_STICKY;

        if (intent == null) {
            MLog.e("开启RemoteControlService服务时，intent对象为空");
            return super.onStartCommand(intent, flags, startId);
        }

        String action = intent.getAction();
        if (Const.START_REMOTE_CONTROL_ACTION.equals(action)) {
            if (eventSendController != null) {
                eventSendController.initEventModel();
            }
            /** 初始化DMS service */
        } else if (Const.UPDATE_INJECT_MODEL_ACTION.equals(action)) {
            if (eventSendController != null) {
                int model = intent.getIntExtra(EventSendController.KEY_INJECT_MODEL, EventSendController.ADB_MODEL);//ADB_MODEL 20150908修改为PLUGIN_MODEL
                eventSendController.setInjectModel(model);
            }
        }

        return super.onStartCommand(intent, flags, startId);
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

//        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

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
     * 开启Socket 服务需要开启一个线程处理，等待客户端连接
     */
    private void initNetty() {


        Log.i(TAG, "=======");
        if (!NettyHelper.getInstance().isServerStart()) {
            new NettyThread().start();
        }

    }

    String vtionVolume = "vtionVolume";

    String[] volumes = null;

    String volume = null;


    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public class NettyThread extends Thread {
        @Override
        public void run() {
            super.run();

            NettyHelper.getInstance().setListener(new NettyListener() {
                @Override
                public void onMessageResponse(Object msg) {

                    Log.d(TAG, "=============" + new String((byte[]) msg));

                    byte[] bs = (byte[]) msg;

                    if (new String(bs).contains(vtionVolume)) {

                        volumes = new String(bs)
                                .split(vtionVolume);

                        volume = volumes[volumes.length - 1];
                        Log.d(TAG, "vtionVolume======" + volume);

                        if (isNumeric(volume)) {

                            audio.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    Integer.parseInt(volume),
                                    AudioManager.FLAG_PLAY_SOUND
                                                | AudioManager.FLAG_SHOW_UI);

                        }

                    } else {

                        dataHandle((byte[]) msg);

                    }

                    //不需要插件直接条用本地adb
//                        instrumentation.sendKeyDownUpSync(Integer.parseInt(reqStr));
//                }

//                    if (!"Heart break".equals(reqStr)) {
//                        if (reqStr.startsWith("vtionVolume")) {
//                            audio.setStreamVolume(
//                                    AudioManager.STREAM_MUSIC,
//                                    Integer.parseInt(reqStr
//                                            .split("vtionVolume")[1]),
//                                    AudioManager.FLAG_PLAY_SOUND
//                                            | AudioManager.FLAG_SHOW_UI);
//                        } else if (reqStr.startsWith("vtionInput")) {
//                            eventSendController.sendInputText(reqStr
//                                    .split("vtionInput")[1]);
//                        } else {
//                            eventSendController.sendEvent(reqStr);
//                        }
//                    }

//                    Toast.makeText(RemoteControlService.this, String.valueOf(msg), Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onChannel(final Channel channel) {
                    //设置通道连接到封装的类中
                    NettyHelper.getInstance().setChannel(channel);

                    Log.i(TAG, "建立连接 onChannel(): " + "接收(" + channel.toString() + ")");

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            tvNetty.setText("接收:(" + channel.toString() + ")");
//
//                        }
//                    });
                }

                @Override
                public void onStartServer() {
                    Log.i(TAG, "Netty Server started 已開啟");
//                    Toast.makeText(RemoteControlService.this, "Netty Server 已開啟", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onStopServer() {
                    Log.i(TAG, "Netty Server started 未連接");
//                    Toast.makeText(RemoteControlService.this, "Netty Server 未連接", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onServiceStatusConnectChanged(final int statusCode) {

                    Log.i(TAG, "Netty Server status:" + statusCode);

//                    sendMsgToClient(vtionVolume  + ":" + audio.getMode() + ":"+ audio.getStreamVolume(AudioManager.STREAM_MUSIC));
//                    Log.i(TAG, "Netty==:" + ExPackageManager.getQueryAppInfoJsonString(CustomApplication.getContext(), false));
//                    sendMsgToClient("AppInfo:" + ExPackageManager.getQueryAppInfoJsonString(CustomApplication.getContext(), false));

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            sendMsgToClient(vtionVolume  + ":" + audio.getMode() + ":"+ audio.getStreamVolume(AudioManager.STREAM_MUSIC));
                            Log.i(TAG, "Netty==:" + LocalAppManager.getQueryAppInfoJsonString(CustomApplication.getContext(), false));
                            sendMsgToClient("AppInfo:" + LocalAppManager.getQueryAppInfoJsonString(CustomApplication.getContext(), false));

                        }
                    }).start();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
//                                Log.i(TAG, "STATUS_CONNECT_SUCCESS:");
//                                //标记连接的状态
//                                vOpenAppGetCode.setSelected(true);
//                            } else {
//                                Log.i(TAG, "onServiceStatusConnectChanged:" + statusCode);
////                                tvNetty.setText("接收:");
//                                vOpenAppGetCode.setSelected(false);
//
//                            }
//                        }
//                    });

                }
            });
            //入口 开启Netty Server
            NettyHelper.getInstance().

                    start();
        }

    }

//是否静音
    private boolean isSilentMode() {
        return audio.getMode() != AudioManager.RINGER_MODE_SILENT;

    }

    /**
     * 服务器端注册一个可供NSD探测到的网络 Ip 地址，便于给展示叫号机连接此socket
     */
    static Runnable nsdServerRunnable = new Runnable() {
        @Override
        public void run() {

            NSDServer nsdServer = new NSDServer();
            nsdServer.startNSDServer(CustomApplication.getContext(), NSD_SERVER_NAME, PORT);

            nsdServer.setRegisterState(new NSDServer.IRegisterState() {
                @Override
                public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                    Log.i(TAG, "已注册服务onServiceRegistered: " + serviceInfo.toString());
//                    Toast.makeText(CustomApplication.getContext(), "已注册服务onServiceRegistered: " + serviceInfo.toString(), Toast.LENGTH_SHORT).show();
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

    public static void registerNsdServer() {

        new Thread(nsdServerRunnable).start();
    }

    public void dataHandle(byte[] bytes) {

        String reqStr = null;
        TlvBox tlvBox = TlvBox.parse(bytes, 0, bytes.length);

        HashMap<Integer, byte[]> mObjects = tlvBox.getmObjects();

        Iterator iterator = mObjects.keySet().iterator();
        if (iterator.hasNext()) {
            Object key = iterator.next();
            Log.d("NettyService_key:", key + "");
            Log.d("NettyService_value:", new String(mObjects.get(key)));
            reqStr = key + "";
            if (Integer.parseInt(reqStr) < KeyValue.KEYVALUE_PLAY_HEARTBEAT) {
                eventSendController.sendEvent(reqStr);
                Log.d("NettyService_reqStr:", "==" + reqStr + "==");
            } else if (Integer.parseInt(reqStr) == KeyValue.KEYVALUE_PLAY_VOLUME_STEP) {
                audio.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        Integer.parseInt(new String(mObjects.get(key))
                                .split("vtionVolume")[1]),
                        AudioManager.FLAG_PLAY_SOUND
                                | AudioManager.FLAG_SHOW_UI);
                Log.d("NettyService_reqStr:", "==" + reqStr + "==");
            }  else if (Integer.parseInt(reqStr) == KeyValue.KEYCODE_APP_OPEN) {//打开应用
                String appPkg = new String(mObjects.get(key));
                LocalAppManager.startNonPartyApplication(CustomApplication.getContext(), appPkg);

            } else if (Integer.parseInt(reqStr) > KeyValue.KEYVALUE_PLAY_HEARTBEAT) {
                TlvBox tlvBox1 = TlvBox.getObjectValue(mObjects, key);
                HashMap<Integer, byte[]> mObjects1 = tlvBox1.getmObjects();
                Iterator iterator1 = mObjects1.keySet().iterator();
                while (iterator1.hasNext()) {
                    Object key1 = iterator1.next();
                    byte[] bytes2 = mObjects1.get(key1);
                    Log.d("NettyService_key1:", key1 + "=====");
                    String str = new String(bytes2);
                    Log.d("NettyService_key1:", str + "=====");
                    if (key1.toString().equals(KeyValue.KEYCODE_PLAY_DEEPLINK + "") || key1.toString().equals(KeyValue.KEYCODE_PLAY_APPSTART + "")) {
                        if (key1.toString().equals(KeyValue.KEYCODE_PLAY_APPSTART + "")) {
                            String[] appsOpen = new String(bytes2).split("&&");
                            if (TextUtils.isEmpty(appsOpen[2])) {
                                RemoteControlService.sendMsgToClient("10304");//应用打开失败
                                return;
                            }
                        }
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(BROADCAST_ACTION);
                        intent.putExtra("msgKey", key1 + "");
                        intent.putExtra("msgValue", new String(bytes2));
                        this.sendBroadcast(intent);
                    }
                    Log.d("NettyService_value222:", new String(bytes2) + "=====");
                }
            } else {
                //Heartbeat

                Log.d("NettyService:", new String(mObjects.get(key)));
            }
        }
    }


    /**
     * 测试发送给客户端的消息
     */
    public static void sendMsgToClient(String receiveMsg) {
        if (!NettyHelper.getInstance().getConnectStatus()) {
//            Toast.makeText(CustomApplication.getContext(), "未连接,请先连接", LENGTH_SHORT).show();
        } else {
            final String msg = "收到回复。。。";
            if (TextUtils.isEmpty(msg.trim())) {
                return;
            }
            NettyHelper.getInstance().sendMsgToServer(receiveMsg, new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        Log.i(TAG, "Write auth successful");

                    } else {
                        Log.i(TAG, "Write auth error");
                    }
                }
            });

        }
    }
    /**
     * 测试发送给客户端的消息
     */
    public static void sendMsgToClient(byte[] receiveMsg) {
        if (!NettyHelper.getInstance().getConnectStatus()) {
//            Toast.makeText(CustomApplication.getContext(), "未连接,请先连接", LENGTH_SHORT).show();
        } else {
            final String msg = "收到回复。。。";
            if (TextUtils.isEmpty(msg.trim())) {
                return;
            }
            NettyHelper.getInstance().sendMsgToServer(receiveMsg, new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        Log.i(TAG, "Write auth successful");

                    } else {
                        Log.i(TAG, "Write auth error");
                    }
                }
            });

        }
    }

}
