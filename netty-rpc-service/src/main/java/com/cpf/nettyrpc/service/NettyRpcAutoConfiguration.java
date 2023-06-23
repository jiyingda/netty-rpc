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

    @Bean
    public RpcServerChannelHandler rpcServerChannelHandler() {
        return new RpcServerChannelHandler();
    }

}

