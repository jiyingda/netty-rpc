package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.RpcHandler;
import com.cpf.nettyrpc.common.RpcRequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiyingdabj
 */
public class RpcHandlerManager {

    @Autowired
    private Map<String, RpcHandler> handlerMap = new HashMap<>();

    private Map<String, Method> methodMap = new HashMap<>();

    @PostConstruct
    private void initMethodMapping() {
        handlerMap.forEach((k, v) -> {
            Method[] methods = v.getClass().getMethods();
            String path = k + "#";
            for (Method method : methods) {
                RpcRequestMapping rpcRequestMapping = method.getAnnotation(RpcRequestMapping.class);
                if (rpcRequestMapping != null) {
                    methodMap.put(path + rpcRequestMapping.path(), method);
                }
            }
        });
    }

    public RpcHandler getHandler(String name) {
        return handlerMap.get(name);
    }

    public Method getMethod(String handler, String method) {
        return methodMap.get(handler + "#" + method);
    }
}
