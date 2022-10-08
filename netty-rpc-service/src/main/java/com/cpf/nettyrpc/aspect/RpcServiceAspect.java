/**
 * @(#)RpcServiceAspect.java, 9月 30, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.aspect;

import com.cpf.nettyrpc.annotation.RpcMethod;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author jiyingdabj
 */
@Order(0)
@Aspect
@Component
public class RpcServiceAspect {

    @Around("@annotation(rpcMethod)")
    public Object proceed(ProceedingJoinPoint pjp, RpcMethod rpcMethod) throws Throwable {
        try {
            System.out.println("========>>>" + rpcMethod.name() + "<<<<=====");;
            Object result = pjp.proceed();
            return result;
        } finally {


        }
    }
}
