package com.cpf.nettyrpc.common;

import java.io.Serializable;

/**
 * @author jiyingdabj
 */
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 278592291450011396L;

    private String requestId;

    private String handler;

    private String method;

    private Class<?>[] classType;

    private Object[] objects;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Class<?>[] getClassType() {
        return classType;
    }

    public void setClassType(Class<?>[] classType) {
        this.classType = classType;
    }

    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
