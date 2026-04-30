package com.cpf.nettyrpc.client;

import com.cpf.nettyrpc.common.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
import java.util.concurrent.TimeUnit;

/**
 * @author jiyingdabj
 */
@Slf4j
public class RpcClientService {

    private static final long DEFAULT_TIMEOUT_MS = 5000;
    private static final long RECONNECT_DELAY_MS = 3000;

    private volatile boolean isInitialized = false;
    private volatile boolean closed = false;

    private final String host;
    private final int port;
    private final long timeoutMs;

    private volatile Channel channel;
    private EventLoopGroup group;
    private Bootstrap bootstrap;

    public RpcClientService(String host, int port) {
        this(host, port, DEFAULT_TIMEOUT_MS);
    }

    public RpcClientService(String host, int port, long timeoutMs) {
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
    }

    public synchronized void init() {
        if (isInitialized) {
            return;
        }

        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
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

        connect();
        log.info("===== rpcClient ready =====");
        isInitialized = true;
    }

    private void connect() {
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
            channel.closeFuture().addListener(f -> {
                if (!closed) {
                    log.warn("RpcClient disconnected, reconnecting in {}ms ...", RECONNECT_DELAY_MS);
                    group.schedule(this::connect, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("RpcClientService connect interrupted", e);
        } catch (Exception e) {
            if (!closed) {
                log.error("RpcClientService connect failed, retrying in {}ms", RECONNECT_DELAY_MS, e);
                group.schedule(this::connect, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void close() {
        closed = true;
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public <T> T sendMessage(String handler, String method, Object... args) {
        log.info("sendMessage {}, method {}, {}", handler, method, args[0]);
        if (!isInitialized) {
            throw new RuntimeException("RpcClientService is not initialized");
        }
        Channel ch = channel;
        if (ch == null || !ch.isActive()) {
            throw new RuntimeException("RpcClient channel is not active, maybe reconnecting");
        }

        String reqId = UUID.randomUUID().toString();
        Promise<Object> promise = new DefaultPromise<>(ch.eventLoop());
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
            ch.writeAndFlush(rpcRequest);
            boolean completed = promise.await(timeoutMs, TimeUnit.MILLISECONDS);
            if (!completed) {
                throw new RuntimeException("RPC timeout after " + timeoutMs + "ms, handler=" + handler + ", method=" + method);
            }
            if (promise.isSuccess()) {
                return (T) promise.getNow();
            } else {
                throw new RuntimeException(promise.cause());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("RPC interrupted", e);
        } finally {
            ClientChannelHandler.requestFeatureMap.remove(reqId);
        }
    }
}
