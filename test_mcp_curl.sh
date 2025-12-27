#!/bin/bash
# Test MCP server with OpenAI Responses API using curl
#
# Usage:
#   export OPENAI_API_KEY="your-api-key"
#   ./test_mcp_curl.sh

set -e

MCP_SERVER_URL="${MCP_SERVER_URL:-https://numerai-finance-production.up.railway.app/mcp}"

echo "============================================================"
echo "TESTING MCP SERVER WITH OPENAI RESPONSES API"
echo "============================================================"
echo ""
echo "MCP Server URL: $MCP_SERVER_URL"
echo ""

if [ -z "$OPENAI_API_KEY" ]; then
    echo "ERROR: OPENAI_API_KEY is not set"
    echo "Please run: export OPENAI_API_KEY='your-api-key'"
    exit 1
fi

# Test 1: Mortgage calculation request
echo "Test 1: Mortgage Payment Calculation"
echo "-------------------------------------"

RESPONSE=$(curl -s "https://api.openai.com/v1/responses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OPENAI_API_KEY" \
  -d "{
    \"model\": \"gpt-4o\",
    \"input\": \"What is the monthly payment for a \$300,000 mortgage at 6.5% for 30 years? Use the calculator tool.\",
    \"tools\": [
      {
        \"type\": \"mcp\",
        \"server_label\": \"numerai_finance\",
        \"server_url\": \"$MCP_SERVER_URL\",
        \"require_approval\": \"never\"
      }
    ]
  }")

echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""
echo ""

# Test 2: Current rates request
echo "Test 2: Current Market Rates"
echo "----------------------------"

RESPONSE=$(curl -s "https://api.openai.com/v1/responses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OPENAI_API_KEY" \
  -d "{
    \"model\": \"gpt-4o\",
    \"input\": \"What are the current mortgage rates? Get them from the finance calculator.\",
    \"tools\": [
      {
        \"type\": \"mcp\",
        \"server_label\": \"numerai_finance\",
        \"server_url\": \"$MCP_SERVER_URL\",
        \"require_approval\": \"never\"
      }
    ]
  }")

echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""
echo ""

# Test 3: Tax estimation
echo "Test 3: Tax Estimation"
echo "----------------------"

RESPONSE=$(curl -s "https://api.openai.com/v1/responses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $OPENAI_API_KEY" \
  -d "{
    \"model\": \"gpt-4o\",
    \"input\": \"Estimate federal taxes for someone earning \$85,000 filing as single.\",
    \"tools\": [
      {
        \"type\": \"mcp\",
        \"server_label\": \"numerai_finance\",
        \"server_url\": \"$MCP_SERVER_URL\",
        \"require_approval\": \"never\"
      }
    ]
  }")

echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

echo "============================================================"
echo "TESTS COMPLETE"
echo "============================================================"
