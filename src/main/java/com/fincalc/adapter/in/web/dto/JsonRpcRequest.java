package com.fincalc.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonRpcRequest(
        String jsonrpc,
        Object id,
        @NotBlank(message = "Method is required")
        String method,
        Map<String, Object> params
) {
    public JsonRpcRequest {
        if (jsonrpc == null) jsonrpc = "2.0";
    }
}
