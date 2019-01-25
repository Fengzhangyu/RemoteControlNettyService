package com.zeasn.remotecontrol.service.netty;

import android.util.Log;

import com.zeasn.remotecontrol.interfaces.NettyListener;
import com.zeasn.remotecontrol.utils.MLog;
import com.zeasn.remotecontrol.utils.TlvBox;

import java.util.HashMap;
import java.util.Iterator;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by Devin.F on 2019/1/17.
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<byte[]> {

    private static final String TAG = NettyServerHandler.class.getSimpleName();
    private NettyListener mListener;

    public NettyServerHandler(NettyListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, byte[] bytes) throws Exception {
        mListener.onMessageResponse(bytes);

//        String s = tlvBox.getStringValue(13);
//        String s = new String(bytes);
//        Log.d("NettyService_Test:", s);
//        mListener.onMessageResponse(s);
    }


    /**
     * 连接成功
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelActive");
        mListener.onChannel(ctx.channel());
        NettyHelper.getInstance().setConnectStatus(true);
        mListener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelInactive");
        NettyHelper.getInstance().setConnectStatus(false);
        mListener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
        // NettyClient.getInstance().reconnect();
    }

}
//
////    @Override
////    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
////    }
//
//    /**
//     * 连接成功
//     *
//     * @param ctx
//     * @throws Exception
//     */
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        Log.e(TAG, "channelActive");
//        mListener.onChannel(ctx.channel());
//        EchoServer.getInstance().setConnectStatus(true);
//        mListener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
//    }
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        Log.e(TAG, "channelInactive");
//        EchoServer.getInstance().setConnectStatus(false);
//        mListener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
//        // NettyClient.getInstance().reconnect();
//    }
//}
