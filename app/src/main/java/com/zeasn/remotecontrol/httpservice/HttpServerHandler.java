package com.zeasn.remotecontrol.httpservice;

import android.content.Context;

import com.zeasn.remotecontrol.CustomApplication;
import com.zeasn.remotecontrol.utils.ExPackageManager;
import com.zeasn.remotecontrol.utils.LocalAppManager;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * Created: devin.feng
 * 2019-08-07
 * Email: devin.feng@zeasn.com
 * Descripe:
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final String containsUri = "pkg=";
    ByteBuf content;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

//        System.out.println("接收到请求：  "+msg);
        if (msg instanceof HttpRequest && ((HttpRequest) msg).getUri().contains(containsUri)) {
            HttpRequest msgs = (HttpRequest) msg;

            System.out.println("接收到请求：  " + msgs.method());
            //pkg=
            String[] strs = msgs.getUri().split(containsUri);
            if (strs.length > 1 && strs[1] != null) {

                List<LocalAppManager.AppInfo> list = LocalAppManager.queryAppInfo(CustomApplication.getContext(), false);

                //设置返回内容
//            ByteBuf content = Unpooled.copiedBuffer("Hello World\n", CharsetUtil.UTF_8);
//            ByteBuf content = Unpooled.copiedBuffer("Hello World\n", CharsetUtil.UTF_8);

//                byte[] content = ExPackageManager.getAppIcon(list.get(0).getPackageName(), CustomApplication.getContext());
                content = Unpooled.copiedBuffer(LocalAppManager.getAppIcon(strs[1], CustomApplication.getContext()));

                //创建响应
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
//                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content);

                ctx.writeAndFlush(response);

            }

        }

    }

}
