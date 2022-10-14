/**
 * @(#)RpcClient.java, 10月 14, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.client;

import java.net.URISyntaxException;

/**
 * @author jiyingdabj
 */
public class RpcClient {

    private RpcClientChannelHandler rpcClientChannelHandler;

    private RpcClientService rpcClientService;

    public RpcClient() {
        this.rpcClientChannelHandler = new RpcClientChannelHandler();
        this.rpcClientService = new RpcClientService(rpcClientChannelHandler);
    }

    public String call(String rpcMethodName, String args) throws URISyntaxException {
        rpcClientService.init();
        return rpcClientChannelHandler.sendMessage(rpcMethodName, args);
    }
}
