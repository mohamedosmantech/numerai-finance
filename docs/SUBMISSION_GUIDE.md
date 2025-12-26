# Numerai Finance - Submission Guide

## Quick Start

### 1. Check Build Status

**Option A: Railway Dashboard**
- Go to: https://railway.com/project/82364e74-1d36-460a-8152-edd3bb3b7611
- Watch the "Deployments" section
- Green checkmark = Build complete

**Option B: Railway CLI**
```bash
railway logs
```

### 2. Generate Public Domain

After build completes:
```bash
railway service    # Select your service
railway domain     # Generate public URL
```

You'll get a URL like: `numerai-finance-production.up.railway.app`

### 3. Verify Endpoints

```bash
# Replace with your Railway domain
export DOMAIN=numerai-finance-production.up.railway.app

# Health check
curl https://$DOMAIN/actuator/health

# Landing page
open https://$DOMAIN

# API docs
open https://$DOMAIN/swagger-ui.html
```

### 4. Test in ChatGPT Developer Mode

1. Go to ChatGPT → Settings → Connectors → Developer Mode
2. Add your MCP server URL: `https://YOUR_DOMAIN/mcp`
3. Test commands:
   - "Calculate my mortgage for $300,000 at 6.5% for 30 years"
   - "How much will $10,000 grow in 20 years at 7% interest?"
   - "Estimate my taxes for $100,000 income in California"

### 5. Submit to App Store

Portal: https://platform.openai.com/apps-manage

| Field | Value |
|-------|-------|
| App Name | Numerai Finance |
| MCP Endpoint | `https://YOUR_DOMAIN/mcp` |
| Transport | SSE |
| Category | Finance |
| Privacy Policy | `https://YOUR_DOMAIN/privacy-policy.html` |

---

## Available Tools

### calculate_loan_payment
Calculate mortgage and loan payments with full amortization.

**Example prompt**: "What's my monthly payment for a $400,000 house at 7% for 30 years?"

### calculate_compound_interest
Project investment growth with compound interest.

**Example prompt**: "If I invest $500/month for 25 years at 8%, how much will I have?"

### estimate_taxes
Estimate US federal and state income taxes.

**Example prompt**: "Calculate my taxes for $85,000 salary in Texas, married filing jointly"

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Build failed | Check Railway logs for errors |
| Health check fails | Verify PORT environment variable |
| Tools not working | Test `/mcp` endpoint directly |
| 403 errors | Check CSP headers configuration |

---

## Project Links

- GitHub: https://github.com/mohamedosmantech/numerai-finance
- Railway: https://railway.com/project/82364e74-1d36-460a-8152-edd3bb3b7611
