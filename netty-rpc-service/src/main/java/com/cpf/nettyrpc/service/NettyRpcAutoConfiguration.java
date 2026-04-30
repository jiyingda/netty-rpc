package com.cpf.nettyrpc.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author jiyingdabj
 */

@Configuration
public class NettyRpcAutoConfiguration implements ApplicationContextAware {

    @Value("${netty.rpc.port:8082}")
    private int rpcPort;

    private ApplicationContext applicationContext;

    @Bean
    public RpcService rpcService() {
        return new RpcService(applicationContext, rpcPort);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

