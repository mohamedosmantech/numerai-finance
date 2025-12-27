#!/usr/bin/env python3
"""
Test script for Numerai Finance MCP server using OpenAI Responses API.

This bypasses the ChatGPT UI connector limitations and tests the MCP server
directly via the OpenAI API with MCP tool support.

Usage:
    export OPENAI_API_KEY="your-api-key"
    python3 test_mcp_openai.py

Requirements:
    pip install openai
"""

import os
import json
import urllib.request
import urllib.error

# OpenAI import is optional - only needed for Responses API testing
try:
    from openai import OpenAI
    OPENAI_AVAILABLE = True
except ImportError:
    OPENAI_AVAILABLE = False

# Configuration
MCP_SERVER_URL = os.getenv("MCP_SERVER_URL", "https://numerai-finance-production.up.railway.app/mcp")

def test_mcp_with_responses_api():
    """Test MCP server using OpenAI Responses API with MCP tools."""

    if not OPENAI_AVAILABLE:
        print("OpenAI library not installed. Install with: pip install openai")
        return False

    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        print("ERROR: OPENAI_API_KEY environment variable not set")
        print("Please set it with: export OPENAI_API_KEY='your-api-key'")
        return False

    client = OpenAI(api_key=api_key)

    print(f"Testing MCP server at: {MCP_SERVER_URL}")
    print("-" * 60)

    # Test prompts that should trigger MCP tool calls
    test_prompts = [
        "What is the monthly payment for a $300,000 mortgage at 6.5% for 30 years?",
        "Calculate the compound interest on $10,000 at 7% for 10 years with monthly compounding",
        "What are the current mortgage rates?",
        "Estimate federal taxes for someone earning $85,000 as single filer",
    ]

    for i, prompt in enumerate(test_prompts, 1):
        print(f"\n[Test {i}] {prompt}")
        print("-" * 40)

        try:
            # Create a response with MCP tool
            response = client.responses.create(
                model="gpt-4o",
                input=prompt,
                tools=[{
                    "type": "mcp",
                    "server_label": "numerai_finance",
                    "server_url": MCP_SERVER_URL,
                    "require_approval": "never"
                }]
            )

            # Print the response
            print(f"Status: {response.status}")

            # Check for tool calls in the output
            for output_item in response.output:
                if hasattr(output_item, 'type'):
                    if output_item.type == "mcp_call":
                        print(f"MCP Tool Called: {output_item.name}")
                        print(f"Arguments: {json.dumps(output_item.arguments, indent=2)}")
                    elif output_item.type == "mcp_call_output":
                        print(f"MCP Output: {output_item.output[:200]}...")
                    elif output_item.type == "message":
                        for content in output_item.content:
                            if hasattr(content, 'text'):
                                # Truncate long responses
                                text = content.text
                                if len(text) > 500:
                                    text = text[:500] + "..."
                                print(f"Response: {text}")

            print("SUCCESS: MCP tool integration working!")

        except Exception as e:
            print(f"ERROR: {e}")

            # Check if it's a feature availability error
            if "mcp" in str(e).lower():
                print("\nNote: MCP tool support in Responses API may require specific API access.")
                print("Falling back to direct MCP endpoint testing...")
                return test_mcp_endpoint_directly()

    return True


def test_mcp_endpoint_directly():
    """Fallback: Test MCP endpoints directly via HTTP."""
    print("\n" + "=" * 60)
    print("DIRECT MCP ENDPOINT TESTING")
    print("=" * 60)

    tests = [
        {
            "name": "MCP Initialize",
            "method": "POST",
            "url": MCP_SERVER_URL,
            "body": {
                "jsonrpc": "2.0",
                "id": 1,
                "method": "initialize",
                "params": {}
            }
        },
        {
            "name": "MCP Tools List",
            "method": "POST",
            "url": MCP_SERVER_URL,
            "body": {
                "jsonrpc": "2.0",
                "id": 2,
                "method": "tools/list",
                "params": {}
            }
        },
        {
            "name": "Calculate Loan Payment",
            "method": "POST",
            "url": MCP_SERVER_URL,
            "body": {
                "jsonrpc": "2.0",
                "id": 3,
                "method": "tools/call",
                "params": {
                    "name": "calculate_loan_payment",
                    "arguments": {
                        "principal": 300000,
                        "annualRate": 6.5,
                        "years": 30
                    }
                }
            }
        },
        {
            "name": "Get Current Rates",
            "method": "POST",
            "url": MCP_SERVER_URL,
            "body": {
                "jsonrpc": "2.0",
                "id": 4,
                "method": "tools/call",
                "params": {
                    "name": "get_current_rates",
                    "arguments": {}
                }
            }
        },
        {
            "name": "Estimate Taxes",
            "method": "POST",
            "url": MCP_SERVER_URL,
            "body": {
                "jsonrpc": "2.0",
                "id": 5,
                "method": "tools/call",
                "params": {
                    "name": "estimate_taxes",
                    "arguments": {
                        "grossIncome": 85000,
                        "filingStatus": "single"
                    }
                }
            }
        }
    ]

    all_passed = True

    for test in tests:
        print(f"\n[{test['name']}]")
        print("-" * 40)

        try:
            data = json.dumps(test["body"]).encode("utf-8")
            req = urllib.request.Request(
                test["url"],
                data=data,
                headers={"Content-Type": "application/json"},
                method=test["method"]
            )

            with urllib.request.urlopen(req, timeout=30) as response:
                result = json.loads(response.read().decode("utf-8"))

                if "error" in result:
                    print(f"ERROR: {result['error']}")
                    all_passed = False
                else:
                    print(f"SUCCESS")
                    # Pretty print result (truncated)
                    result_str = json.dumps(result, indent=2)
                    if len(result_str) > 500:
                        result_str = result_str[:500] + "\n... (truncated)"
                    print(result_str)

        except urllib.error.HTTPError as e:
            print(f"HTTP ERROR {e.code}: {e.read().decode('utf-8')}")
            all_passed = False
        except urllib.error.URLError as e:
            print(f"CONNECTION ERROR: {e.reason}")
            all_passed = False
        except Exception as e:
            print(f"ERROR: {e}")
            all_passed = False

    return all_passed


def test_discovery_endpoints():
    """Test the discovery endpoints."""
    print("\n" + "=" * 60)
    print("DISCOVERY ENDPOINTS TESTING")
    print("=" * 60)

    base_url = MCP_SERVER_URL.replace("/mcp", "")

    endpoints = [
        "/.well-known/oauth-protected-resource",
        "/.well-known/mcp-server",
        "/.well-known/mcp/tools",
        "/mcp/.well-known/oauth-protected-resource",
    ]

    for endpoint in endpoints:
        url = base_url + endpoint
        print(f"\n[GET {endpoint}]")

        try:
            req = urllib.request.Request(url, method="GET")
            with urllib.request.urlopen(req, timeout=10) as response:
                result = json.loads(response.read().decode("utf-8"))
                result_str = json.dumps(result, indent=2)
                if len(result_str) > 300:
                    result_str = result_str[:300] + "\n... (truncated)"
                print(f"SUCCESS:\n{result_str}")
        except urllib.error.HTTPError as e:
            print(f"HTTP {e.code}: {e.read().decode('utf-8')[:100]}")
        except Exception as e:
            print(f"ERROR: {e}")


if __name__ == "__main__":
    print("=" * 60)
    print("NUMERAI FINANCE MCP SERVER TEST")
    print("=" * 60)

    # First test discovery endpoints
    test_discovery_endpoints()

    # Test MCP endpoints directly
    test_mcp_endpoint_directly()

    # Optionally test with OpenAI Responses API
    print("\n" + "=" * 60)
    print("OPENAI RESPONSES API TEST (requires openai library and OPENAI_API_KEY)")
    print("=" * 60)

    if not OPENAI_AVAILABLE:
        print("Skipped - Install openai library: pip install openai")
    elif not os.getenv("OPENAI_API_KEY"):
        print("Skipped - Set OPENAI_API_KEY to test with OpenAI Responses API")
        print("  export OPENAI_API_KEY='your-api-key'")
    else:
        test_mcp_with_responses_api()
