/**
 * @(#)RpcService.java, 9月 28, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author jiyingdabj
 */
@Component
public class RpcService {

    @Autowired
    private RpcHandlerManager rpcHandlerManager;

    @PostConstruct
    private void init() {
        System.out.println("start run RpcService");
        new Thread(() -> {
            //构造两个线程组
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                //服务端启动辅助类
                ServerBootstrap bootstrap = new ServerBootstrap();

                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {

                            @Override
                            protected void initChannel(SocketChannel sc) throws Exception {
                                ChannelPipeline pipeline = sc.pipeline();
                                //处理http消息的编解码
                                pipeline.addLast("httpServerCodec", new HttpServerCodec());
                                //添加自定义的ChannelHandler
                                pipeline.addLast("httpServerHandler", new HttpServerHandler(rpcHandlerManager));
                            }
                        });

                ChannelFuture future = bootstrap.bind(8082).sync();
                //等待服务端口关闭
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // 优雅退出，释放线程池资源
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }
}
