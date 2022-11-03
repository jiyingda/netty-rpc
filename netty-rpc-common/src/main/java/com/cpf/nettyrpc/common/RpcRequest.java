package com.cpf.nettyrpc.common;

import java.io.Serializable;

/**
 * @author jiyingdabj
 */
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 278592291450011396L;

    private String requestId;

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
}
