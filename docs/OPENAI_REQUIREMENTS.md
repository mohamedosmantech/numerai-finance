# OpenAI ChatGPT Apps - Requirements Checklist

**Submission Portal**: https://platform.openai.com/apps-manage

---

## Implementation Status

### Technical Requirements
| Requirement | Status | Details |
|-------------|--------|---------|
| MCP Protocol | Done | JSON-RPC 2.0 over SSE |
| Tool Definitions | Done | 3 tools with JSON Schema |
| `_meta` in responses | Done | OpenAI visibility metadata |
| `.well-known` endpoints | Done | OAuth & MCP discovery |
| CSP Headers | Done | ChatGPT embedding allowed |
| Health Checks | Done | `/actuator/health` |

### Tool Configuration
| Tool | readOnlyHint | destructiveHint |
|------|--------------|-----------------|
| calculate_loan_payment | true | false |
| calculate_compound_interest | true | false |
| estimate_taxes | true | false |

### Compliance
| Requirement | Status |
|-------------|--------|
| No digital product sales | OK |
| No adult/gambling content | OK |
| No ads | OK |
| General audience (13+) | OK |
| Minimum data collection | OK |

---

## Required Endpoints

### OAuth Discovery
```
GET /.well-known/oauth-protected-resource
```

### MCP Endpoints
```
GET /mcp                    - SSE connection
POST /mcp/messages          - JSON-RPC handler
```

### Health Check
```
GET /actuator/health        - Server status
```

---

## Deployment Checklist

- [x] Railway project created
- [x] GitHub repo connected
- [ ] Build completed
- [ ] Public domain generated
- [ ] Test all endpoints over HTTPS
- [ ] Test in ChatGPT Developer Mode

---

## Testing Commands

```bash
# Health check
curl https://YOUR_DOMAIN/actuator/health

# OAuth discovery
curl https://YOUR_DOMAIN/.well-known/oauth-protected-resource

# MCP metadata
curl https://YOUR_DOMAIN/.well-known/mcp-server
```

---

## App Store Listing

| Field | Value |
|-------|-------|
| App Name | Numerai Finance |
| Category | Finance / Productivity |
| Description | Smart financial intelligence - mortgage payments, investment growth, and tax estimation |
| Privacy Policy | https://YOUR_DOMAIN/privacy-policy.html |
