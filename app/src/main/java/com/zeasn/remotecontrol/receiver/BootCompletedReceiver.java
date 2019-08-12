package com.zeasn.remotecontrol.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.zeasn.remotecontrol.broadcast.NetWorkStateReceiver;
import com.zeasn.remotecontrol.httpservice.HttpServerlInitializer;
import com.zeasn.remotecontrol.utils.Const;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * 开机启动广播
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    NetWorkStateReceiver mNetworkStateReceiver;

    //负责接收客户端连接
    private static EventLoopGroup boosGroup;
    //处理连接
    private static EventLoopGroup workerGroup;
    private static ServerBootstrap bootstrap;

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

            startHttpService();


        }
    }

    public static void startHttpService() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if(boosGroup == null)
                        boosGroup = new NioEventLoopGroup();
                    //处理连接
                    if(workerGroup == null)
                        workerGroup = new NioEventLoopGroup();
                    if(bootstrap == null)
                        bootstrap = new ServerBootstrap();

                    bootstrap.group(boosGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new HttpServerlInitializer());

                    //绑定端口号
                    ChannelFuture channelFuture = null;
                    try {
                        channelFuture = bootstrap.bind(9090).sync();
                        channelFuture.channel().closeFuture().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } finally {
                    boosGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();

                }
            }
        }).start();
    }

    public void registerNetWorkReceiver(Context context) {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mNetworkStateReceiver = new NetWorkStateReceiver();
        context.registerReceiver(mNetworkStateReceiver, filter);
    }

}
