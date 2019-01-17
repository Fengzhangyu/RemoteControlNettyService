package com.zeasn.remotecontrol.service.netty;

import com.zeasn.remotecontrol.interfaces.NettyListener;
import com.zeasn.remotecontrol.utils.MLog;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

/**
 * Created by Devin.F on 2019/1/16.
 */
public class NettyHelper {

    private static final String TAG = "NettyHelper";
    private final int port = 5051;

    private Channel channel;

    private static NettyHelper instance = null;
    private NettyListener listener;
    private boolean connectStatus;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private boolean isServerStart;

    public static NettyHelper getInstance() {
        if (instance == null) {
            synchronized (NettyHelper.class) {
                if (instance == null) {
                    instance = new NettyHelper();
                }
            }
        }
        return instance;
    }

    private NettyHelper() {
    }

    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            MLog.d("initChannel ch:" + ch);
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            //发送消息到客户端
                            ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                            ch.pipeline().addLast(new ByteArrayDecoder());
                            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            ch.pipeline().addLast(new NettyServerHandler(listener));
                        }
                    });

            // Bind and start to accept incoming connections.
            //调用connect发起异步连接操作，然后调用sync同步方法等待连接成功。
            ChannelFuture f = bootstrap.bind().sync(); // 8

            MLog.d(NettyHelper.class.getName() + " started and listen on " + f.channel().localAddress());
            isServerStart = true;
            listener.onStartServer();
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            //等待客户端链路关闭，当客户端连接关闭之后，客户端主函数退出，退出之前释放NIO线程组的资源
            f.channel().closeFuture().sync(); // 9

        } catch (InterruptedException e) {
            MLog.d(e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            isServerStart = false;
            listener.onStopServer();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void disconnect() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public void setListener(NettyListener listener) {
        this.listener = listener;
    }

    public void setConnectStatus(boolean connectStatus) {
        this.connectStatus = connectStatus;
    }

    public boolean getConnectStatus() {
        return connectStatus;
    }

    public boolean isServerStart() {
        return isServerStart;
    }

    public boolean sendMsgToServer(String data, ChannelFutureListener listener) {
        boolean flag = channel != null && connectStatus && channel.isActive();
        if (flag) {
//			ByteBuf buf = Unpooled.copiedBuffer(data);
//            ByteBuf byteBuf = Unpooled.copiedBuffer(data + System.getProperty("line.separator"), //2
//                    CharsetUtil.UTF_8);
            channel.writeAndFlush(data + System.getProperty("line.separator")).addListener(listener);
        }
        return flag;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

}



