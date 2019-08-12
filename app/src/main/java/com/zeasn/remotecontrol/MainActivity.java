package com.zeasn.remotecontrol;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zeasn.remotecontrol.broadcast.NetWorkStateReceiver;
import com.zeasn.remotecontrol.event.EventSendController;
import com.zeasn.remotecontrol.httpservice.HttpServerlInitializer;
import com.zeasn.remotecontrol.receiver.BootCompletedReceiver;
import com.zeasn.remotecontrol.service.RemoteControlService;
import com.zeasn.remotecontrol.utils.Const;
import com.zeasn.remotecontrol.utils.ExPackageManager;
import com.zeasn.remotecontrol.utils.LocalAppManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


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

        BootCompletedReceiver.startHttpService();

//        registerNetWorkReceiver();

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                List<ExPackageManager.AppInfo> list = ExPackageManager.queryAppInfo(MainActivity.this, false);
//
//                Log.d("test", "test" + list.size());
//                RemoteControlService.sendMsgToClient(ExPackageManager.getQueryAppInfoJsonString(MainActivity.this, false));


                LocalAppManager.startNonPartyApplication(MainActivity.this, "com.netflix.mediaclient");


//                //负责接收客户端连接
//                EventLoopGroup boosGroup = new NioEventLoopGroup();
//                //处理连接
//                EventLoopGroup workerGroup = new NioEventLoopGroup();
//
//                try {
//                    ServerBootstrap bootstrap = new ServerBootstrap();
//
//                    bootstrap.group(boosGroup, workerGroup)
//                            .channel(NioServerSocketChannel.class)
//                            .childHandler(new HttpServerlInitializer());
//
//                    //绑定端口号
//                    ChannelFuture channelFuture = null;
//                    try {
//                        channelFuture = bootstrap.bind(9090).sync();
//                        channelFuture.channel().closeFuture().sync();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                } finally {
//                    boosGroup.shutdownGracefully();
//                    workerGroup.shutdownGracefully();
//
//                }

            }
        });

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
