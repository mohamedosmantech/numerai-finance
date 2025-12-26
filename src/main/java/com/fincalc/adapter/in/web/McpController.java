package com.fincalc.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincalc.adapter.config.ChatGptRequestContext;
import com.fincalc.adapter.in.web.dto.JsonRpcRequest;
import com.fincalc.adapter.in.web.dto.JsonRpcResponse;
import com.fincalc.application.AnalyticsService;
import com.fincalc.application.McpToolHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@Tag(name = "MCP", description = "Model Context Protocol endpoints for financial calculations")
public class McpController {

    private static final String PROTOCOL_VERSION = "2024-11-05";
    private final Map<String, SseEmitter> sessions = new ConcurrentHashMap<>();

    private final McpToolHandler toolHandler;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<ChatGptRequestContext> requestContextProvider;
    private final AnalyticsService analyticsService;

    @Operation(
            summary = "Establish MCP connection",
            description = "Opens a Server-Sent Events (SSE) connection for MCP communication. Returns an endpoint URL for sending messages."
    )
    @ApiResponse(responseCode = "200", description = "SSE connection established")
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        String sessionId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(0L); // No timeout

        sessions.put(sessionId, emitter);
        log.info("New MCP session established: {}", sessionId);

        // Track MCP session
        analyticsService.trackMcpSession();

        emitter.onCompletion(() -> cleanup(sessionId, "completed"));
        emitter.onTimeout(() -> cleanup(sessionId, "timed out"));
        emitter.onError(e -> cleanup(sessionId, "error: " + e.getMessage()));

        try {
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data("/mcp/messages?sessionId=" + sessionId));
        } catch (IOException e) {
            log.error("Failed to send endpoint event for session {}", sessionId, e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Operation(
            summary = "Send JSON-RPC message",
            description = "Sends a JSON-RPC 2.0 request to the MCP server. Supports methods: initialize, tools/list, tools/call"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Request accepted and processed",
                    content = @Content(schema = @Schema(implementation = JsonRpcResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PostMapping("/messages")
    public ResponseEntity<JsonRpcResponse> handleMessage(
            @Parameter(description = "Session ID from SSE connection") @RequestParam String sessionId,
            @Valid @RequestBody JsonRpcRequest request
    ) {
        log.debug("Received request for session {}: method={}", sessionId, request.method());

        // Track MCP request
        analyticsService.trackMcpRequest();

        SseEmitter emitter = sessions.get(sessionId);
        if (emitter == null) {
            log.warn("Unknown session: {}", sessionId);
            return ResponseEntity.notFound().build();
        }

        JsonRpcResponse response = processRequest(request);
        sendSseResponse(emitter, response);

        return ResponseEntity.accepted().body(response);
    }

    private JsonRpcResponse processRequest(JsonRpcRequest request) {
        String method = request.method();
        Object id = request.id();
        Map<String, Object> params = request.params() != null ? request.params() : Map.of();

        try {
            return switch (method) {
                case "initialize" -> handleInitialize(id);
                case "notifications/initialized" -> JsonRpcResponse.success(id, Map.of());
                case "tools/list" -> handleToolsList(id);
                case "tools/call" -> handleToolsCall(id, params);
                case "resources/list" -> JsonRpcResponse.success(id, Map.of("resources", List.of()));
                case "resources/templates/list" -> JsonRpcResponse.success(id, Map.of("resourceTemplates", List.of()));
                default -> {
                    log.warn("Unknown method: {}", method);
                    yield JsonRpcResponse.methodNotFound(id, method);
                }
            };
        } catch (IllegalArgumentException e) {
            log.warn("Invalid params for method {}: {}", method, e.getMessage());
            return JsonRpcResponse.invalidParams(id, e.getMessage());
        } catch (Exception e) {
            log.error("Error processing method {}", method, e);
            return JsonRpcResponse.internalError(id, e.getMessage());
        }
    }

    private JsonRpcResponse handleInitialize(Object id) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities", Map.of("tools", Map.of()));
        result.put("serverInfo", Map.of(
                "name", "fincalc-pro",
                "version", "1.0.0"
        ));
        return JsonRpcResponse.success(id, result);
    }

    private JsonRpcResponse handleToolsList(Object id) {
        return JsonRpcResponse.success(id, Map.of("tools", toolHandler.getToolDefinitions()));
    }

    @SuppressWarnings("unchecked")
    private JsonRpcResponse handleToolsCall(Object id, Map<String, Object> params) {
        String toolName = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());

        if (toolName == null || toolName.isBlank()) {
            return JsonRpcResponse.invalidParams(id, "Tool name is required");
        }

        // Get request context for ChatGPT headers (country, language)
        ChatGptRequestContext context = requestContextProvider.getIfAvailable();
        Map<String, Object> result = toolHandler.executeTool(toolName, arguments, context);
        return JsonRpcResponse.success(id, result);
    }

    private void sendSseResponse(SseEmitter emitter, JsonRpcResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            emitter.send(SseEmitter.event().name("message").data(json));
        } catch (IOException e) {
            log.error("Failed to send SSE response", e);
        }
    }

    private void cleanup(String sessionId, String reason) {
        sessions.remove(sessionId);
        log.info("MCP session {}: {}", sessionId, reason);
    }

    @Operation(summary = "Get active session count", description = "Returns the number of active MCP sessions (for monitoring)")
    @ApiResponse(responseCode = "200", description = "Session count retrieved")
    @GetMapping("/sessions/count")
    public ResponseEntity<Map<String, Integer>> getSessionCount() {
        return ResponseEntity.ok(Map.of("activeSessions", sessions.size()));
    }

    // ============= OAuth Discovery Endpoints (relative to /mcp) =============

    private static final String BASE_URL = "https://numerai-finance-production.up.railway.app";

    @Operation(summary = "OAuth Protected Resource Metadata (at /mcp path)",
            description = "Returns OAuth 2.0 protected resource metadata for MCP discovery")
    @GetMapping(value = "/.well-known/oauth-protected-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> mcpOAuthProtectedResource() {
        log.info("OAuth protected resource metadata requested at /mcp path");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("resource", BASE_URL + "/mcp");
        response.put("authorization_servers", List.of());
        response.put("bearer_methods_supported", List.of());
        response.put("resource_documentation", "https://github.com/numerai-finance/fincalc-pro");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "OAuth Authorization Server Metadata (at /mcp path)",
            description = "Returns OAuth 2.0 authorization server metadata")
    @GetMapping(value = "/.well-known/oauth-authorization-server", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> mcpOAuthAuthorizationServer() {
        log.info("OAuth authorization server metadata requested at /mcp path");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("issuer", BASE_URL);
        response.put("response_types_supported", List.of());
        response.put("grant_types_supported", List.of());
        response.put("scopes_supported", List.of());
        response.put("token_endpoint_auth_methods_supported", List.of());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "OpenID Configuration (at /mcp path)",
            description = "Returns OpenID Connect discovery metadata")
    @GetMapping(value = "/.well-known/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> mcpOpenIdConfiguration() {
        log.info("OpenID configuration requested at /mcp path");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("issuer", BASE_URL);
        response.put("response_types_supported", List.of());
        response.put("grant_types_supported", List.of());
        response.put("scopes_supported", List.of());
        response.put("subject_types_supported", List.of("public"));

        return ResponseEntity.ok(response);
    }
}
