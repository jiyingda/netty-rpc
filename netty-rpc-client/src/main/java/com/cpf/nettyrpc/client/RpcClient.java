package com.cpf.nettyrpc.client;

/**
 * @author jiyingdabj
 */
public class RpcClient {

    private RpcClientService rpcClientService;

    public RpcClient(String host, int port) {
        this.rpcClientService = new RpcClientService(host, port);
        this.rpcClientService.init();
    }

    public String call(String handler, String method, Object... args) {
        return rpcClientService.sendMessage(handler, method, args);
    }
}
