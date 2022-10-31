/**
 * @(#)RpcClient.java, 10月 14, 2022.
 * <p>
 * Copyright 2022 . All rights reserved.
 *  PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
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

    public String call(String path, String args) throws URISyntaxException {
        return rpcClientService.sendMessage(path, args);
    }
}
