package com.fincalc.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Well-known endpoints for OAuth 2.1 and MCP discovery.
 * Required by OpenAI ChatGPT Apps for authentication flow.
 */
@RestController
@RequestMapping("/.well-known")
@Tag(name = "Discovery", description = "OAuth and MCP discovery endpoints")
public class WellKnownController {

    @Value("${fincalc.oauth.resource-url:https://fincalc.example.com}")
    private String resourceUrl;

    @Value("${fincalc.oauth.auth-server:}")
    private String authorizationServer;

    @Value("${fincalc.oauth.docs-url:https://github.com/fincalc/fincalc-pro}")
    private String documentationUrl;

    @Operation(
            summary = "OAuth Protected Resource Metadata",
            description = "Returns metadata about this MCP server for OAuth discovery (RFC 9728)"
    )
    @GetMapping(value = "/oauth-protected-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getOAuthProtectedResource() {
        var response = new java.util.LinkedHashMap<String, Object>();
        response.put("resource", resourceUrl);

        // Only include authorization_servers if OAuth is configured
        if (authorizationServer != null && !authorizationServer.isBlank()) {
            response.put("authorization_servers", List.of(authorizationServer));
        }

        response.put("scopes_supported", List.of("calculate:read"));
        response.put("resource_documentation", documentationUrl);

        return response;
    }

    @Operation(
            summary = "MCP Server Metadata",
            description = "Returns metadata about this MCP server capabilities"
    )
    @GetMapping(value = "/mcp-server", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getMcpServerMetadata() {
        return Map.of(
                "name", "fincalc-pro",
                "version", "1.0.0",
                "description", "Financial Calculator MCP Server",
                "protocol_version", "2024-11-05",
                "capabilities", Map.of(
                        "tools", true,
                        "resources", false,
                        "prompts", false
                ),
                "tools", List.of(
                        "calculate_loan_payment",
                        "calculate_compound_interest",
                        "estimate_taxes"
                )
        );
    }
}
