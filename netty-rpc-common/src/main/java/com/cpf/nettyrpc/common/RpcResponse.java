package com.cpf.nettyrpc.common;

import java.io.Serializable;

/**
 * @author jiyingdabj
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 5748865593720926764L;

    private String requestId;

    private Object content;

    private boolean success;

    private String exception;

    public RpcResponse() {
    }

    public RpcResponse(String requestId, Object content) {
        this.requestId = requestId;
        this.content = content;
        this.success = true;
    }

    public static RpcResponse error(String requestId, String exception) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setSuccess(false);
        response.setException(exception);
        return response;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
