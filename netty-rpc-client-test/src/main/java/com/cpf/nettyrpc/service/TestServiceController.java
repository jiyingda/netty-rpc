package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.client.RpcClient;

/**
 * @author jiyingdabj
 */
public class TestServiceController {

    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("localhost", 8082);
        Thread t1 = new Thread(() -> {

            String s = rpcClient.call("testRpcHandler", "name", "aaa", "bbb");
            System.out.println("T1 print =  " + s);
        });

        Thread t2 = new Thread(() -> {
            String s = rpcClient.call("testRpcHandler", "name", "ccc", "ddd");
            System.out.println("T2 print =  " + s);
        });
        t1.start();
        t2.start();

    }
}
