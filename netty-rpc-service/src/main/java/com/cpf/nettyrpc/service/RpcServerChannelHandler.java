package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.RpcHandler;
import com.cpf.nettyrpc.common.RpcRequest;
import com.cpf.nettyrpc.common.RpcRequestMapping;
import com.cpf.nettyrpc.common.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiyingdabj
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcServerChannelHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private volatile int init = 0;
    private final Object lock = new Object();

    private final ApplicationContext applicationContext;
    public RpcServerChannelHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        // RpcRequest rpcRequest = JsonUtils.readValue(msg, new TypeReference<RpcRequest>() {});
        if (rpcRequest == null) {
            return;
        }
        initHandler();
        RpcHandler handler = getHandler(rpcRequest.getHandler());
        Method method = getMethod(rpcRequest.getHandler() + "/" + rpcRequest.getMethod());
        log.info("httpServerHandler rpcName = {}, method = {}", rpcRequest.getHandler(), rpcRequest.getMethod());
        if (handler != null && method != null) {
            RpcResponse response = RpcHandlerProxy.invoke(handler, method, rpcRequest);
            log.info("httpServerHandler method-invoke = {}", response);
            ctx.writeAndFlush(response);
        } else {
            RpcResponse response = new RpcResponse();
            response.setRequestId(rpcRequest.getRequestId());
            response.setSuccess(false);
            response.setException("method not found");
            ctx.writeAndFlush(response);
        }

    }

    public RpcHandler getHandler(String name) {
        return handlerMap.get(name);
    }

    public Method getMethod(String path) {
        return methodMap.get(path);
    }

    private Map<String, RpcHandler> handlerMap = new HashMap<>();

    private Map<String, Method> methodMap = new HashMap<>();

    private void initHandler() {
        if (init == 1) {
            return;
        }
        synchronized (lock) {
            if (init == 0) {
                applicationContext.getBeansOfType(RpcHandler.class).values().forEach(e -> {
                    RpcRequestMapping rpcRequestMappingHandler = e.getClass().getAnnotation(RpcRequestMapping.class);
                    if (rpcRequestMappingHandler != null) {
                        handlerMap.put(rpcRequestMappingHandler.path(), e);
                        Method[] methods = e.getClass().getMethods();
                        for (Method method : methods) {
                            RpcRequestMapping rpcRequestMapping = method.getAnnotation(RpcRequestMapping.class);
                            if (rpcRequestMapping != null) {
                                methodMap.put(rpcRequestMappingHandler.path() + "/" + rpcRequestMapping.path(), method);
                            }
                        }
                    }
                });
                init = 1;
            }
        }
    }
}
