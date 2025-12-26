# OpenAI ChatGPT Apps - Requirements & Submission Checklist

**Submission Portal**: https://platform.openai.com/apps-manage
**Guidelines**: https://developers.openai.com/apps-sdk/app-submission-guidelines/

---

## ✅ Implementation Status

### Technical Requirements
| Requirement | Status | Details |
|-------------|--------|---------|
| MCP Protocol | ✅ Done | JSON-RPC 2.0 over SSE |
| Tool Definitions | ✅ Done | 3 tools with JSON Schema |
| `content` response | ✅ Done | Markdown text responses |
| `structuredContent` response | ✅ Done | JSON data for widgets |
| `_meta` in responses | ✅ Done | OpenAI-specific metadata |
| `.well-known` endpoints | ✅ Done | OAuth & MCP discovery |
| CSP Headers | ✅ Done | ChatGPT embedding allowed |
| Security Schemes | ✅ Done | `noauth` on all tools |
| Health Checks | ✅ Done | `/actuator/health` |
| HTTPS Ready | ✅ Done | Production config |

### Tool Annotations (Critical for Approval)
| Tool | readOnlyHint | destructiveHint | openWorldHint |
|------|--------------|-----------------|---------------|
| calculate_loan_payment | ✅ true | ✅ false | N/A |
| calculate_compound_interest | ✅ true | ✅ false | N/A |
| estimate_taxes | ✅ true | ✅ false | N/A |

### Compliance
| Requirement | Status | Notes |
|-------------|--------|-------|
| No digital product sales | ✅ OK | Free calculation tools |
| No adult/gambling/drugs | ✅ OK | Financial calculators only |
| No ads | ✅ OK | No advertising |
| General audience (13+) | ✅ OK | Educational finance tools |
| Minimum data collection | ✅ OK | Only calculation inputs |
| No PII/PHI/credentials | ✅ OK | No sensitive data collected |

---

## OpenAI MCP Response Format

### Required Response Structure
```json
{
  "content": [
    { "type": "text", "text": "Markdown response for chat" }
  ],
  "structuredContent": {
    "input": { ... },
    "result": { ... }
  },
  "_meta": {
    "openai/outputTemplate": "ui://widget/calculator.html",
    "openai/widgetAccessible": true,
    "openai/visibility": "public"
  }
}
```

### Tool Definition with Security
```json
{
  "name": "calculate_loan_payment",
  "description": "...",
  "inputSchema": { ... },
  "annotations": {
    "destructiveHint": false,
    "readOnlyHint": true
  },
  "securitySchemes": {
    "type": "noauth"
  }
}
```

---

## Required Endpoints

### 1. OAuth Protected Resource Metadata
```
GET /.well-known/oauth-protected-resource

Response:
{
  "resource": "https://fincalc.example.com",
  "authorization_servers": ["https://auth.example.com"],
  "scopes_supported": ["calculate:read"],
  "resource_documentation": "https://docs.fincalc.example.com"
}
```

### 2. MCP Endpoints (Current)
- `GET /mcp` - SSE connection
- `POST /mcp/messages` - JSON-RPC handler

---

## Authentication Options

### Option A: No Auth (Simplest - for public tools)
- Set `securitySchemes: { "type": "noauth" }` on each tool
- No OAuth setup needed
- **Recommended for FinCalc Pro** (read-only calculations)

### Option B: OAuth 2.1 (For user-specific data)
Requires:
1. Integration with Auth0/Okta/Cognito
2. DCR endpoint
3. Token verification
4. PKCE support

---

## Submission Checklist

### Account & Verification
- [x] Organization verified on OpenAI Platform
- [ ] Account has Owner role
- [ ] Use Chrome browser (Safari has issues)

### Technical (All Done)
- [x] `.well-known/oauth-protected-resource` endpoint
- [x] `.well-known/mcp-server` endpoint
- [x] Tools return `_meta` with OpenAI fields
- [x] `securitySchemes: noauth` on all tools
- [x] `readOnlyHint: true` on all tools (read-only calculators)
- [x] `destructiveHint: false` on all tools
- [x] CSP headers configured
- [x] Error responses with proper format

### Deployment (Pending)
- [ ] Deploy to public HTTPS domain
- [ ] Set `FINCALC_RESOURCE_URL` to your domain
- [ ] Verify endpoints work over HTTPS
- [ ] Test with ChatGPT Developer Mode

### App Store Listing (Needed for Submission)
- [ ] **App Name**: FinCalc Pro
- [ ] **Description**: Professional financial calculators for loans, investments, and taxes
- [ ] **Category**: Finance / Productivity
- [ ] **Privacy Policy URL**: (create and host)
- [ ] **Support Contact**: (your email)
- [ ] **Screenshots**: (capture from Swagger UI or actual usage)

### Privacy Policy Must Include
- [ ] What data is collected (calculation inputs only)
- [ ] How data is used (calculations only, not stored)
- [ ] No data shared with third parties
- [ ] No cookies or tracking
- [ ] Contact information

---

## Quick Deployment Commands

```bash
# Build and push Docker image
make docker-build
docker tag fincalc-pro:latest your-registry/fincalc-pro:latest
docker push your-registry/fincalc-pro:latest

# Or deploy to Kubernetes
kubectl apply -f k8s/

# Set environment for production
export FINCALC_RESOURCE_URL=https://your-domain.com
export SPRING_PROFILES_ACTIVE=prod
```

---

## Testing Before Submission

1. **Verify endpoints**:
```bash
curl https://your-domain.com/.well-known/oauth-protected-resource
curl https://your-domain.com/.well-known/mcp-server
curl https://your-domain.com/actuator/health
```

2. **Test in ChatGPT Developer Mode**:
   - Go to ChatGPT Settings → Connectors → Developer Mode
   - Add your MCP server URL
   - Test all 3 tools

---

## Reference Links
- [Submission Portal](https://platform.openai.com/apps-manage)
- [App Guidelines](https://developers.openai.com/apps-sdk/app-submission-guidelines/)
- [Build MCP Server](https://developers.openai.com/apps-sdk/build/mcp-server/)
- [Submit App](https://developers.openai.com/apps-sdk/deploy/submission)
- [Community Post](https://community.openai.com/t/chatgpt-app-store-is-open-for-submissions/1369611)
