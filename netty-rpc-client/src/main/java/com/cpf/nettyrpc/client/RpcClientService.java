/**
 * @(#)RpcClientService.java, 10月 12, 2022.
 * <p>
 * Copyright 2022 . All rights reserved.
 *  PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.client;

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
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author jiyingdabj
 */
@Slf4j
public class RpcClientService {

    private volatile int inited = 0;

    private ChannelHandlerContext channelHandlerContext;

    private String responseMsg;

    private CountDownLatch countDownLatch;

    private final String host;

    private final int port;

    public RpcClientService(String host, int port) {
        this.host = host;
        this.port = port;
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
                                        responseMsg = buf.toString(io.netty.util.CharsetUtil.UTF_8);
                                        countDownLatch.countDown();
                                        log.info("channelRead0 {}", responseMsg);
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
        log.info("==== rpcClient ready =====");
    }

    public String sendMessage(String path, String args) throws URISyntaxException {
        init();
        countDownLatch = new CountDownLatch(1);
        URI uri = new URI(path);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                uri.toASCIIString(), Unpooled.wrappedBuffer(args.getBytes(StandardCharsets.UTF_8)));

        // 构建http请求
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        // 发送http请求
        channelHandlerContext.channel().writeAndFlush(request);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("get response msg = {}", responseMsg);
        return responseMsg;
    }
}
