package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.RpcHandler;
import com.cpf.nettyrpc.common.RpcRequestMapping;
import org.springframework.stereotype.Service;

/**
 * @author jiyingdabj
 */

@Service(value = "testRpcHandler")
public class TestRpcHandler implements RpcHandler {

    @RpcRequestMapping(path = "name")
    public String name(String s1, String s2) {
        return "TestRpcHandler " + s1 + " -> " + s2 + " -> " + System.currentTimeMillis();
    }
}
