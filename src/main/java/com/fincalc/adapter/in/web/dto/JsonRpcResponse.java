package com.fincalc.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonRpcResponse(
        String jsonrpc,
        Object id,
        Object result,
        JsonRpcError error
) {
    public record JsonRpcError(int code, String message, Object data) {
        public JsonRpcError(int code, String message) {
            this(code, message, null);
        }
    }

    public static JsonRpcResponse success(Object id, Object result) {
        return new JsonRpcResponse("2.0", id, result, null);
    }

    public static JsonRpcResponse error(Object id, int code, String message) {
        return new JsonRpcResponse("2.0", id, null, new JsonRpcError(code, message));
    }

    public static JsonRpcResponse parseError() {
        return error(null, -32700, "Parse error");
    }

    public static JsonRpcResponse invalidRequest(Object id) {
        return error(id, -32600, "Invalid request");
    }

    public static JsonRpcResponse methodNotFound(Object id, String method) {
        return error(id, -32601, "Method not found: " + method);
    }

    public static JsonRpcResponse invalidParams(Object id, String message) {
        return error(id, -32602, "Invalid params: " + message);
    }

    public static JsonRpcResponse internalError(Object id, String message) {
        return error(id, -32603, "Internal error: " + message);
    }
}
