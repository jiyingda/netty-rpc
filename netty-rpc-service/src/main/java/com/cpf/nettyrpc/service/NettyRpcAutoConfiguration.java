/**
 * @(#)NettyRpcAutoConfig.java, 9月 29, 2022.
 * <p>
 * Copyright 2022 . All rights reserved.
 *  PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.cpf.nettyrpc.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author jiyingdabj
 */

@Configuration
public class NettyRpcAutoConfiguration {

    @Bean
    public RpcService rpcService() {
        return new RpcService();
    }

    @Bean
    public RpcHandlerManager rpcHandlerManager() {
        return new RpcHandlerManager();
    }

}

