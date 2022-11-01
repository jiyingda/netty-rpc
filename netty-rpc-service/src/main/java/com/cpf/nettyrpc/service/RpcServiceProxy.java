/**
 * @(#)RpcServiceProxy.java, 10月 31, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.JsonUtils;
import com.cpf.nettyrpc.common.RpcRequest;
import com.cpf.nettyrpc.common.RpcHandler;
import com.cpf.nettyrpc.common.RpcResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author jiyingdabj
 */
public class RpcServiceProxy {

    public static RpcResponse invoke(RpcHandler handler, Method method, String content) throws InvocationTargetException, IllegalAccessException {
        RpcRequest rpcRequest = JsonUtils.readValue(content, new TypeReference<RpcRequest>() {});
        assert rpcRequest != null;

        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());
        response.setContent(method.invoke(handler, rpcRequest.getObjects()));
        return response;

    }
}
