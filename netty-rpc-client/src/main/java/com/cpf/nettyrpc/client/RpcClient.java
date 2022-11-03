package com.cpf.nettyrpc.client;

import java.net.URISyntaxException;

/**
 * @author jiyingdabj
 */
public class RpcClient {

    private RpcClientService rpcClientService;

    public RpcClient(String host, int port) {
        this.rpcClientService = new RpcClientService(host, port);
    }

    public String call(String path, Object... args) throws URISyntaxException {
        return rpcClientService.sendMessage(path, args);
    }
}
