package com.cpf.nettyrpc.client;

import com.cpf.nettyrpc.common.JsonUtils;
import com.cpf.nettyrpc.common.RpcRequest;
import com.cpf.nettyrpc.common.RpcResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author jiyingdabj
 */
@Slf4j
public class RpcClientService {

    private volatile boolean isInitialized = false;

    private final String host;

    private final int port;

    private Channel channel;

    private final ConcurrentHashMap<String, CountDownLatch> requestCountDownLatchMap;

    private final ConcurrentHashMap<String, RpcResponse> requestFeatureMap;

    public RpcClientService(String host, int port) {
        this.host = host;
        this.port = port;
        this.requestCountDownLatchMap = new ConcurrentHashMap<>();
        this.requestFeatureMap = new ConcurrentHashMap<>();
    }

    private synchronized void init() {
        if (isInitialized) {
            return;
        }
        new Thread(() -> {
            EventLoopGroup group = new NioEventLoopGroup();

            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new StringDecoder());
                                pipeline.addLast(new StringEncoder());
                                pipeline.addLast(new SimpleChannelInboundHandler<String>() {

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
                                        RpcResponse response = JsonUtils.readValue(msg, new TypeReference<RpcResponse>() {});
                                        if (response != null) {
                                            String requestId = response.getRequestId();
                                            if (requestCountDownLatchMap.containsKey(requestId)) {
                                                requestFeatureMap.put(requestId, response);
                                                requestCountDownLatchMap.get(requestId).countDown();
                                                log.info("channelRead0 {}", msg);
                                            } else {
                                                log.warn("channelRead0-feature is closed {}", msg);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                // 启动客户端.
                ChannelFuture f = b.connect(host, port).sync();
                channel = f.channel();
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        }).start();
        int max = 32;
        while (channel == null && max > 0) {
            log.info("rpcClient not ready");
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                log.error("", e);
            } finally {
                max--;
            }
        }
        log.info("===== rpcClient ready =====");
        isInitialized = true;
    }

    public <T> T sendMessage(String handler, String method, Object... args) {
        if (!isInitialized) {
            init();
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        String reqId = UUID.randomUUID().toString();
        requestCountDownLatchMap.put(reqId, countDownLatch);
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
        channel.writeAndFlush(JsonUtils.writeValue(rpcRequest));
        try {
            boolean f = countDownLatch.await(5, TimeUnit.SECONDS);
            if (f) {
                RpcResponse response = requestFeatureMap.get(reqId);
                log.info("get response msg = {}", response.getContent());
                return (T)response.getContent();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            requestCountDownLatchMap.remove(reqId);
            requestFeatureMap.remove(reqId);
        }
        return null;
    }
}
