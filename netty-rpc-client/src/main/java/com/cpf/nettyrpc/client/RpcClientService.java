/**
 * @(#)RpcClientService.java, 10月 12, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author jiyingdabj
 */
@Slf4j
public class RpcClientService {

    private volatile int inited = 0;

    private final RpcClientChannelHandler rpcClientChannelHandler;

    public RpcClientService(RpcClientChannelHandler handler) {
         this.rpcClientChannelHandler = handler;
    }

    public synchronized void init() {
        if (inited > 0) {
            return;
        }
        inited = 1;
        new Thread(() -> {
            String host = "127.0.0.1";
            int port = 8082;

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
                                pipeline.addLast(rpcClientChannelHandler);
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
        while (!rpcClientChannelHandler.isReady()) {
            log.info("rpcClient not ready");
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("==== rpcClient ready =====");
    }
}
