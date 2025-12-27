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

    @Value("${app.keycloak.issuer:https://keycloak-production-86b1.up.railway.app/realms/mcp}")
    private String keycloakIssuer;

    private final com.fincalc.application.McpToolHandler mcpToolHandler;

    public WellKnownController(com.fincalc.application.McpToolHandler mcpToolHandler) {
        this.mcpToolHandler = mcpToolHandler;
    }

    /**
     * OAuth 2.0 Protected Resource Metadata (RFC 9470)
     * Points to Keycloak as the authorization server for OAuth flow.
     */
    @Operation(
            summary = "OAuth Protected Resource Metadata",
            description = "Returns OAuth 2.0 protected resource metadata for MCP discovery (RFC 9470). Points to Keycloak for authentication."
    )
    @GetMapping(value = "/oauth-protected-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getOAuthProtectedResource() {
        log.info("OAuth protected resource metadata requested");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("resource", baseUrl + "/mcp");
        // No authentication required (public access)
        response.put("authorization_servers", List.of());
        response.put("bearer_methods_supported", List.of());
        response.put("scopes_supported", List.of());
        response.put("resource_documentation", "https://github.com/numerai-finance/fincalc-pro");

        return ResponseEntity.ok(response);
    }

    /**
     * OAuth Authorization Server Metadata (RFC 8414)
     * Redirects to Keycloak's authorization server metadata.
     */
    @Operation(
            summary = "OAuth Authorization Server Metadata",
            description = "Redirects to Keycloak authorization server metadata (RFC 8414)"
    )
    @GetMapping(value = "/oauth-authorization-server", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> getOAuthAuthorizationServer() {
        log.info("OAuth authorization server metadata requested - redirecting to Keycloak");

        // Redirect to Keycloak's well-known endpoint
        return ResponseEntity.status(302)
                .header("Location", keycloakIssuer + "/.well-known/openid-configuration")
                .build();
    }

    /**
     * OpenID Connect Discovery
     * Redirects to Keycloak's OpenID configuration.
     */
    @Operation(
            summary = "OpenID Connect Discovery",
            description = "Redirects to Keycloak OpenID Connect discovery metadata"
    )
    @GetMapping(value = "/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> getOpenIdConfiguration() {
        log.info("OpenID configuration requested - redirecting to Keycloak");

        // Redirect to Keycloak's well-known endpoint
        return ResponseEntity.status(302)
                .header("Location", keycloakIssuer + "/.well-known/openid-configuration")
                .build();
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

        // CRITICAL: Enable full actions support
        response.put("supports_full_actions", true);
        response.put("disable_auto_invocation", false);
        response.put("keywords_for_triggering", List.of(
                "mortgage", "loan payment", "calculate mortgage", "home loan",
                "compound interest", "investment calculator", "savings calculator",
                "tax estimate", "income tax", "tax calculator", "federal tax",
                "interest rate", "finance calculator", "market rates"
        ));

        // OAuth authentication via Keycloak
        Map<String, Object> auth = new LinkedHashMap<>();
        auth.put("type", "oauth2");
        auth.put("authorization_server", keycloakIssuer);
        auth.put("description", "OAuth 2.1 authentication via Keycloak");
        response.put("authentication", auth);

        response.put("capabilities", Map.of(
                "tools", true,
                "resources", false,
                "prompts", false
        ));
        response.put("actions", List.of(
                "calculate_loan_payment",
                "calculate_compound_interest",
                "estimate_taxes",
                "get_current_rates"
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * MCP Tools Discovery Endpoint
     * Returns full tool definitions with schemas for connector registration.
     */
    /**
     * OpenAI Apps Domain Verification
     */
    @Operation(
            summary = "OpenAI Apps Domain Verification",
            description = "Returns verification token for OpenAI Apps submission"
    )
    @GetMapping(value = "/openai-apps-challenge", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getOpenAiAppsChallenge() {
        log.info("OpenAI Apps domain verification requested");
        return ResponseEntity.ok("bKcb_1D7vMz3tKzY2Crmk_UfGB8a9y_IOqQMXKUgYKc");
    }

    @Operation(
            summary = "MCP Tools Discovery",
            description = "Returns full tool definitions with parameter schemas for ChatGPT connector registration"
    )
    @GetMapping(value = "/mcp/tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getMcpTools() {
        log.info("MCP tools discovery requested");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "Numerai Finance");
        response.put("supports_full_actions", true);
        response.put("disable_auto_invocation", false);
        response.put("keywords_for_triggering", List.of(
                "mortgage", "loan payment", "calculate mortgage", "home loan",
                "compound interest", "investment calculator", "savings calculator",
                "tax estimate", "income tax", "tax calculator", "federal tax",
                "interest rate", "finance calculator", "market rates"
        ));

        // Get full tool definitions with schemas
        response.put("tools", mcpToolHandler.getToolDefinitions());

        // Build action_param_schemas map
        Map<String, Object> actionParamSchemas = new LinkedHashMap<>();
        for (var tool : mcpToolHandler.getToolDefinitions()) {
            String name = (String) tool.get("name");
            Object schema = tool.get("inputSchema");
            if (name != null && schema != null) {
                actionParamSchemas.put(name, schema);
            }
        }
        response.put("action_param_schemas", actionParamSchemas);

        return ResponseEntity.ok(response);
    }
}
