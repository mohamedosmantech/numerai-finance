package com.fincalc.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Well-known endpoints for OAuth 2.0/2.1 and MCP discovery.
 * Required by OpenAI ChatGPT Apps for authentication flow.
 *
 * For public MCP servers (no authentication required), this returns
 * metadata indicating the resource is publicly accessible via empty
 * authorization_servers array.
 */
@Slf4j
@RestController
@RequestMapping("/.well-known")
@CrossOrigin(origins = "*")
@Tag(name = "Discovery", description = "OAuth and MCP discovery endpoints")
public class WellKnownController {

    @Value("${app.base-url:https://numerai-finance-production.up.railway.app}")
    private String baseUrl;

    /**
     * OAuth 2.0 Protected Resource Metadata (RFC 9470)
     * For public (no-auth) servers, returns empty authorization_servers array.
     * This explicitly tells ChatGPT that no authentication is required.
     */
    @Operation(
            summary = "OAuth Protected Resource Metadata",
            description = "Returns OAuth 2.0 protected resource metadata for MCP discovery (RFC 9470). Empty authorization_servers indicates public access."
    )
    @GetMapping(value = "/oauth-protected-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getOAuthProtectedResource() {
        log.info("OAuth protected resource metadata requested");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("resource", baseUrl + "/mcp");
        // Empty array explicitly indicates NO authentication required (public access)
        response.put("authorization_servers", List.of());
        response.put("bearer_methods_supported", List.of());
        response.put("resource_documentation", "https://github.com/numerai-finance/fincalc-pro");

        return ResponseEntity.ok(response);
    }

    /**
     * OAuth Authorization Server Metadata (RFC 8414)
     * For no-auth servers, returns minimal metadata.
     */
    @Operation(
            summary = "OAuth Authorization Server Metadata",
            description = "Returns OAuth 2.0 authorization server metadata (RFC 8414)"
    )
    @GetMapping(value = "/oauth-authorization-server", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getOAuthAuthorizationServer() {
        log.info("OAuth authorization server metadata requested");

        // For a public server, return minimal metadata indicating no auth flow
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("issuer", baseUrl);
        response.put("response_types_supported", List.of());
        response.put("grant_types_supported", List.of());
        response.put("scopes_supported", List.of());
        response.put("token_endpoint_auth_methods_supported", List.of());

        return ResponseEntity.ok(response);
    }

    /**
     * OpenID Connect Discovery (for completeness)
     */
    @Operation(
            summary = "OpenID Connect Discovery",
            description = "Returns OpenID Connect discovery metadata"
    )
    @GetMapping(value = "/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getOpenIdConfiguration() {
        log.info("OpenID configuration requested");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("issuer", baseUrl);
        response.put("response_types_supported", List.of());
        response.put("grant_types_supported", List.of());
        response.put("scopes_supported", List.of());
        response.put("subject_types_supported", List.of("public"));

        return ResponseEntity.ok(response);
    }

    /**
     * MCP Server Metadata
     */
    @Operation(
            summary = "MCP Server Metadata",
            description = "Returns metadata about this MCP server capabilities"
    )
    @GetMapping(value = "/mcp-server", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getMcpServerMetadata() {
        log.info("MCP server metadata requested");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "Numerai Finance");
        response.put("version", "1.0.0");
        response.put("description", "Professional financial calculator for mortgages, taxes, and exchange rates");
        response.put("mcp_endpoint", baseUrl + "/mcp");
        response.put("protocol_version", "2024-11-05");

        // Explicitly declare no authentication required
        Map<String, Object> auth = new LinkedHashMap<>();
        auth.put("type", "noauth");
        auth.put("description", "This MCP server is publicly accessible");
        response.put("authentication", auth);

        response.put("capabilities", Map.of(
                "tools", true,
                "resources", false,
                "prompts", false
        ));
        response.put("tools", List.of(
                "calculate_loan_payment",
                "calculate_compound_interest",
                "estimate_taxes",
                "get_exchange_rate",
                "list_currencies",
                "list_countries",
                "convert_currency"
        ));

        return ResponseEntity.ok(response);
    }
}
