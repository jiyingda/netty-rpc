package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.client.RpcClient;

/**
 * @author jiyingdabj
 */
public class TestServiceController {

    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("localhost", 8082);
        Thread t = new Thread(() -> {
            String s = rpcClient.call("testRpcHandler", "name", "aaa", "bbb");
            System.out.println(s);
        });
        Thread t1 = new Thread(() -> {
            String s = rpcClient.call("testRpcHandler", "name", "ccccc", "ddd");
            System.out.println(s);
        });
        t.start();
        t1.start();
        System.out.println("-----");
    }
}
