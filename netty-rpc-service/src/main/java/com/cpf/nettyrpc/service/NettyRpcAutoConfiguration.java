/**
 * @(#)NettyRpcAutoConfig.java, 9月 29, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiyingdabj
 */
@Configuration
public class NettyRpcAutoConfiguration {

    @Bean(name = "rpcService")
    public RpcService rpcService() {
        return new RpcService();
    }
}
