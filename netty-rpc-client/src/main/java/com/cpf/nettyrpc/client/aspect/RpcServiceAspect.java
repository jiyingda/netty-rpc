/**
 * @(#)RpcServiceAspect.java, 9月 30, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.client.aspect;

import com.cpf.nettyrpc.client.annotation.RpcMethod;
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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * @author jiyingdabj
 */
@Slf4j
@Order(0)
@Aspect
@Component
public class RpcServiceAspect {

    @Around("@annotation(rpcMethod)")
    public Object proceed(ProceedingJoinPoint pjp, RpcMethod rpcMethod) throws Throwable {
        log.info("RpcServiceAspect#rpcMethod = {}", rpcMethod.name());
        Object result = pjp.proceed();
        sendMessage();
        return result;
    }

    private void sendMessage() throws InterruptedException {
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
                            pipeline.addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    URI uri = new URI("http://127.0.0.1:8082");
                                    String msg = "Are you ok?";
                                    FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                                            uri.toASCIIString(), Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));

                                    // 构建http请求
                                    request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
                                    request.headers().set("rpcName", "123");
                                    // 发送http请求
                                    ctx.channel().writeAndFlush(request);
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse msg) throws Exception {
                                    msg.headers().get(HttpHeaderNames.CONTENT_TYPE);
                                    ByteBuf buf = msg.content();
                                    System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
                                }
                            });
                        }
                    });

            // 启动客户端.
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}