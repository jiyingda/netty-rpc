/**
 * @(#)NettyRpcAutoConfig.java, 9月 29, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.client.aspect.RpcServiceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


/**
 * @author jiyingdabj
 */

@Configuration
@EnableAspectJAutoProxy
public class NettyRpcAutoConfiguration {

    @Bean
    public RpcService rpcService() {
        return new RpcService();
    }

    @Bean
    public RpcServiceAspect rpcServiceAspect() {
        return new RpcServiceAspect();
    }

    @Bean
    public RpcHandlerManager rpcHandlerManager() {
        return new RpcHandlerManager();
    }

}

