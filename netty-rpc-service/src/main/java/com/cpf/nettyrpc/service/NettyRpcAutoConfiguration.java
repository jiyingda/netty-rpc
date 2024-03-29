package com.cpf.nettyrpc.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author jiyingdabj
 */

@Configuration
public class NettyRpcAutoConfiguration implements ApplicationContextAware  {

    private ApplicationContext applicationContext;

    @Bean
    public RpcService rpcService() {
        return new RpcService(applicationContext);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

