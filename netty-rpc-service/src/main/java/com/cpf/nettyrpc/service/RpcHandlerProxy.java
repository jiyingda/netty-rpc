package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.JsonUtils;
import com.cpf.nettyrpc.common.RpcHandler;
import com.cpf.nettyrpc.common.RpcRequest;
import com.cpf.nettyrpc.common.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author jiyingdabj
 */
@Slf4j
public class RpcHandlerProxy {

    public static RpcResponse invoke(RpcHandler handler, Method method, RpcRequest rpcRequest) throws InvocationTargetException, IllegalAccessException {
        assert rpcRequest != null;
        RpcResponse response = new RpcResponse();
        if (method.getParameterTypes().length != rpcRequest.getClassType().length) {
            response.setSuccess(false);
            response.setException("参数表不一致");
            log.info("response = {}", JsonUtils.writeValue(response));
            return response;
        }
        for (int i = 0; i < rpcRequest.getClassType().length; i++) {
            if (!rpcRequest.getClassType()[i].equals(method.getParameterTypes()[i])) {
                response.setSuccess(false);
                response.setException("参数类型不一致");
                log.info("response = {}", JsonUtils.writeValue(response));
                return response;
            }
        }
        response.setSuccess(true);
        response.setRequestId(rpcRequest.getRequestId());
        response.setContent(method.invoke(handler, rpcRequest.getObjects()));
        log.info("response = {}", JsonUtils.writeValue(response));
        return response;

    }
}
