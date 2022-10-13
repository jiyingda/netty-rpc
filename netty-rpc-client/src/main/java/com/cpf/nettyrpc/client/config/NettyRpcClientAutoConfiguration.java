/**
 * @(#)NettyRpcClientAutoConfiguration.java, 10月 09, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.client.config;

import com.cpf.nettyrpc.client.ClientChannelHandler;
import com.cpf.nettyrpc.client.RpcClientService;
import com.cpf.nettyrpc.client.aspect.RpcServiceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author jiyingdabj
 */
@Configuration
@EnableAspectJAutoProxy
public class NettyRpcClientAutoConfiguration {

    @Bean
    public RpcServiceAspect rpcServiceAspect() {
        return new RpcServiceAspect();
    }

    @Bean
    public RpcClientService rpcClientService() {
        return new RpcClientService();
    }

    @Bean
    public ClientChannelHandler channelHandler() {
        return new ClientChannelHandler();
    }
}
