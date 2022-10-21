/**
 * @(#)RpcHandlerManager.java, 9月 30, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.RpcHandler;
import com.cpf.nettyrpc.common.RpcRequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiyingdabj
 */
@Component
public class RpcHandlerManager {

    @Autowired
    private Map<String, RpcHandler> handlerMap = new HashMap<>();

    private Map<String, Method> methodMap = new HashMap<>();

    @PostConstruct
    private void initMethodMapping() {
        handlerMap.values().forEach(e -> {
            Method[] methods = e.getClass().getMethods();
            for (Method method : methods) {
                RpcRequestMapping rpcRequestMapping = method.getAnnotation(RpcRequestMapping.class);
                if (rpcRequestMapping != null) {
                    methodMap.put(rpcRequestMapping.path(), method);
                }
            }
        });
    }

    public RpcHandler getHandler(String name) {
        return handlerMap.get(name);
    }

    public Method getMethod(String path) {
        return methodMap.get(path);
    }
}
