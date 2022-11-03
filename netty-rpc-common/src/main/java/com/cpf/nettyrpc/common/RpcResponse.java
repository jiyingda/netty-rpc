package com.cpf.nettyrpc.common;

import java.io.Serializable;

/**
 * @author jiyingdabj
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 5748865593720926764L;

    private String requestId;

    private Object content;

    public RpcResponse() {
    }

    public RpcResponse(String requestId, Object content) {
        this.requestId = requestId;
        this.content = content;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
