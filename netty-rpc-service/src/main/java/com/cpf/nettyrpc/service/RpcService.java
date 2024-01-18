package com.cpf.nettyrpc.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;

/**
 * @author jiyingdabj
 */
@Slf4j
public class RpcService  {

    private final ApplicationContext applicationContext;

    public RpcService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void init() {
        log.info("start run RpcService");
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
                                pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                                pipeline.addLast(new ObjectEncoder());
                                //添加自定义的ChannelHandler
                                pipeline.addLast(new RpcServerChannelHandler(applicationContext));
                            }
                        });

                ChannelFuture future = bootstrap.bind(8082).sync();
                //等待服务端口关闭
                future.channel().closeFuture().addListener(f -> {
                    log.info("shutdownGracefully");
                    // 优雅退出，释放线程池资源
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                });
                log.info("netty service is ready");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
