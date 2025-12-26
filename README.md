# Numerai Finance

**Smart Financial Intelligence for ChatGPT** - Professional-grade financial calculations powered by AI.

## Features

- **Mortgage & Loan Analysis** - Calculate monthly payments, total interest, and amortization schedules
- **Investment Growth Projector** - Compound interest with optional monthly contributions for wealth planning
- **Smart Tax Estimator (2024)** - US federal and state income tax calculations with all filing statuses

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+

### Run Locally
```bash
./mvnw spring-boot:run
```

Server starts at `http://localhost:8002`

### Run Tests
```bash
./mvnw test
```

### Build Docker Image
```bash
docker build -t numerai-finance .
docker run -p 8002:8002 numerai-finance
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/mcp` | GET | SSE connection for MCP protocol |
| `/mcp/messages?sessionId=xxx` | POST | JSON-RPC messages |
| `/actuator/health` | GET | Health check |
| `/swagger-ui.html` | GET | API Documentation |
| `/widget/index.html` | GET | Interactive demo |

## MCP Tools

### calculate_loan_payment
```json
{
  "principal": 300000,
  "annualRate": 6.5,
  "years": 30
}
```

### calculate_compound_interest
```json
{
  "principal": 10000,
  "annualRate": 7,
  "years": 20,
  "compoundingFrequency": 12,
  "monthlyContribution": 500
}
```

### estimate_taxes
```json
{
  "grossIncome": 100000,
  "filingStatus": "single",
  "deductions": 0,
  "state": "CA"
}
```

## Deployment

### Railway (Recommended)
```bash
railway init && railway up
```

### Docker Compose
```bash
docker-compose up --build
```

## Architecture

Built with **Hexagonal Architecture** (Ports & Adapters) following SOLID principles.

See [docs/ARCHITECTURE_EXPLAINED.md](docs/ARCHITECTURE_EXPLAINED.md) for details.

## License

MIT
