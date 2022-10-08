/**
 * @(#)RpcHandlerManager.java, 9月 30, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.RpcHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiyingdabj
 */
@Component
public class RpcHandlerManager {

    @Autowired
    private Map<String, RpcHandler> handlerMap = new HashMap<>();

    public RpcHandler getHandler(String name) {
        return handlerMap.get(name);
    }
}
