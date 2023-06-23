package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.client.RpcClient;

/**
 * @author jiyingdabj
 */
public class TestServiceController {

    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("localhost", 8082);
        String s = rpcClient.call("testRpcHandler", "name", "aaa", "bbb");
        System.out.println(s);
    }
}
