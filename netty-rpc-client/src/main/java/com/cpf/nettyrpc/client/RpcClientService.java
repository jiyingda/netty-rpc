/**
 * @(#)RpcClientService.java, 10月 12, 2022.
 * <p>
 * Copyright 2022 . All rights reserved.
 *  PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.client;

import com.cpf.nettyrpc.common.JsonUtils;
import com.cpf.nettyrpc.common.RpcRequest;
import com.cpf.nettyrpc.common.RpcResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author jiyingdabj
 */
@Slf4j
public class RpcClientService {

    private volatile int inited = 0;

    private ChannelHandlerContext channelHandlerContext;

    private final String host;

    private final int port;

    private final ConcurrentHashMap<String, CountDownLatch> requestCountDownLatchMap;

    private final ConcurrentHashMap<String, RpcResponse> requestFeatureMap;

    public RpcClientService(String host, int port) {
        this.host = host;
        this.port = port;
        this.requestCountDownLatchMap = new ConcurrentHashMap<>();
        this.requestFeatureMap = new ConcurrentHashMap<>();
    }

    public synchronized void init() {
        if (inited > 0) {
            return;
        }
        inited = 1;
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
                                pipeline.addLast(new HttpClientCodec());
                                pipeline.addLast(new HttpObjectAggregator(65536));
                                pipeline.addLast(new SimpleChannelInboundHandler<FullHttpResponse>(){

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        channelHandlerContext = ctx;
                                        log.info("ClientChannelHandler#channelActive");
                                    }

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse msg) throws Exception {
                                        channelHandlerContext.channel();
                                        msg.headers().get(HttpHeaderNames.CONTENT_TYPE);
                                        ByteBuf buf = msg.content();
                                        String responseMsg = buf.toString(CharsetUtil.UTF_8);
                                        RpcResponse response = JsonUtils.readValue(responseMsg, new TypeReference<RpcResponse>() {});
                                        if (response != null) {
                                            requestFeatureMap.put(response.getRequestId(), response);
                                            requestCountDownLatchMap.get(response.getRequestId()).countDown();
                                            log.info("channelRead0 {}", responseMsg);
                                        }
                                    }
                                });
                            }
                        });

                // 启动客户端.
                ChannelFuture f = b.connect(host, port).sync();
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        }).start();
        while (channelHandlerContext == null) {
            log.info("rpcClient not ready");
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("===== rpcClient ready =====");
    }

    public <T> T sendMessage(String path, Object... args) throws URISyntaxException {
        init();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String reqId = UUID.randomUUID().toString();
        requestCountDownLatchMap.put(reqId, countDownLatch);
        RpcRequest rpcRequest = new RpcRequest();
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        rpcRequest.setClassType(classes);
        rpcRequest.setObjects(args);
        rpcRequest.setRequestId(reqId);
        URI uri = new URI(path);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                uri.toASCIIString(), Unpooled.wrappedBuffer(JsonUtils.writeValue(rpcRequest).getBytes(StandardCharsets.UTF_8)));

        // 构建http请求
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        // 发送http请求
        channelHandlerContext.channel().writeAndFlush(request);
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
