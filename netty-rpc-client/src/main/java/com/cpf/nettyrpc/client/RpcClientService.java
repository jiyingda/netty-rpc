package com.cpf.nettyrpc.client;

import com.cpf.nettyrpc.common.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author jiyingdabj
 */
@Slf4j
public class RpcClientService {

    private volatile boolean isInitialized = false;

    private final String host;

    private final int port;

    private Channel channel;

    public RpcClientService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void init() {
        if (isInitialized) {
            return;
        }

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ClientChannelHandler());
                        }
                    });
            // 启动客户端.
            channel = b.connect(host, port).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("===== rpcClient ready =====");
        isInitialized = true;
    }

    public <T> T sendMessage(String handler, String method, Object... args) {
        log.info("sendMessage {}, method {}, {}", handler, method, args[0]);
        if (!isInitialized) {
            throw new RuntimeException();
        }

        String reqId = UUID.randomUUID().toString();
        Promise<Object> promise = new DefaultPromise<>(channel.eventLoop());
        ClientChannelHandler.requestFeatureMap.put(reqId, promise);
        RpcRequest rpcRequest = new RpcRequest();
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        rpcRequest.setHandler(handler);
        rpcRequest.setMethod(method);
        rpcRequest.setClassType(classes);
        rpcRequest.setObjects(args);
        rpcRequest.setRequestId(reqId);
        try {
            channel.writeAndFlush(rpcRequest);
            Promise<Object> response = ClientChannelHandler.requestFeatureMap.get(reqId);
            response.await();
            if (response.isSuccess()) {
                return (T)response.getNow();
            } else {
                throw new RuntimeException(response.cause());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException();
        } finally {
            ClientChannelHandler.requestFeatureMap.remove(reqId);
        }
    }
}
