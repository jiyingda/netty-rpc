/**
 * @(#)RpcServiceProxy.java, 10月 31, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.RpcHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author jiyingdabj
 */
public class RpcServiceProxy {

    public static Object invoke(RpcHandler handler, Method method, String param) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(handler, param);

    }
}
