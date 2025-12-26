# Numerai Finance - API Reference

## Base URL
- Local: `http://localhost:8002`
- Production: `https://YOUR_RAILWAY_DOMAIN`

---

## Why Numerai Finance vs Raw AI?

| Feature | Raw ChatGPT | Numerai Finance |
|---------|-------------|-----------------|
| Calculation Accuracy | May hallucinate | Guaranteed (BigDecimal) |
| Tax Brackets | Training data (old) | 2025 official rates |
| Interest Rates | Static estimates | Live from Federal Reserve |
| Multi-language | Limited | 6 languages (admin managed) |
| Audit Trail | None | Full logging |
| API Access | Not available | REST + MCP |
| Custom Branding | No | Yes (admin dashboard) |
| Countries | Generic | US, UK, CA, DE, AU (extensible) |
| Currencies | Limited | 15+ (admin managed) |

---

## Health & Discovery Endpoints

### GET /actuator/health
Check server health status.

### GET /.well-known/oauth-protected-resource
OAuth 2.0 protected resource metadata.

### GET /.well-known/mcp-server
MCP server capabilities.

---

## MCP Tools (4 Tools)

### 1. calculate_loan_payment
Calculate mortgage and loan payments with amortization.

```json
{
  "principal": 300000,
  "annualRate": 6.5,
  "years": 30
}
```

### 2. calculate_compound_interest
Project investment growth with compound interest.

```json
{
  "principal": 10000,
  "annualRate": 7,
  "years": 20,
  "compoundingFrequency": 12,
  "monthlyContribution": 500
}
```

### 3. estimate_taxes
Estimate US federal and state income taxes.

```json
{
  "grossIncome": 100000,
  "filingStatus": "single",
  "deductions": 0,
  "state": "CA"
}
```

### 4. get_current_rates
Get live market rates from Federal Reserve.

```json
{}
```

---

## Admin API

### Countries & Regions
- `GET /api/admin/config/countries` - List all countries
- `POST /api/admin/config/countries` - Add/update country
- `DELETE /api/admin/config/countries/{code}` - Delete country

### Currencies
- `GET /api/admin/config/currencies` - List all currencies
- `POST /api/admin/config/currencies` - Add/update currency

### Rate Providers
- `GET /api/admin/config/rate-providers` - List providers
- `PATCH /api/admin/config/rate-providers/{id}/enable` - Enable/disable

### Localized Messages
- `GET /api/admin/config/messages` - List all messages
- `POST /api/admin/config/messages` - Add/update message

### Response Templates
- `GET /api/admin/config/templates` - List templates
- `POST /api/admin/config/templates` - Customize output format

---

## Market Rates API

### GET /api/rates
Get all current market rates.

**Response:**
```json
{
  "lastUpdated": "2025-12-26",
  "source": "Federal Reserve Economic Data (FRED)",
  "rates": {
    "mortgage30Year": 6.85,
    "mortgage15Year": 6.10,
    "primeRate": 8.50,
    "federalFundsRate": 5.33
  }
}
```

### GET /api/rates/mortgage
Get current mortgage rates only.

### GET /api/rates/federal
Get Federal Reserve rates only.

---

## Supported Countries

| Country | Currency | Tax System | Rate Source |
|---------|----------|------------|-------------|
| US | USD | IRS 2025 | FRED |
| UK | GBP | HMRC 2025/26 | Bank of England |
| Canada | CAD | CRA 2025 | Bank of Canada |
| Germany | EUR | BZSt 2025 | ECB |
| Australia | AUD | ATO 2024-25 | RBA |

*Easily extensible via Admin API*

---

## Supported Currencies

USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, MXN, BRL, AED, SAR, SGD, KRW

*Add more via Admin API*

---

## Error Responses

All errors return user-friendly, localized messages:

```json
{
  "error": {
    "code": -32602,
    "message": "The interest rate 75% is outside the valid range. Most rates are between 3% and 15%. Please enter a rate between 0.01% and 50%."
  }
}
```

---

## Rate Limiting

- **Limit**: 60 requests per minute per IP
- **Header**: `X-RateLimit-Remaining`
