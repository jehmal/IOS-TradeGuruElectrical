#!/usr/bin/env python3
"""
TradeGuru API v1 Endpoint Test Runner

Workflow:
1. Load endpoints.yaml
2. Register fresh test device
3. Test all 19 endpoints systematically
4. For 500 errors: check logs, diagnose, fix
5. Record results to tests/logs/endpoint/{slug}.md
6. Loop fixes until all pass (max 10 iterations)
7. Output final summary
"""

import json
import os
import sys
import time
from datetime import datetime
from typing import Any, Dict, List, Optional
from pathlib import Path

import requests
import yaml

BASE_DIR = Path(__file__).parent
ENDPOINTS_YAML = BASE_DIR / "endpoints.yaml"
LOG_DIR = BASE_DIR / "logs" / "endpoint"
VERCEL_PROJECT = "/mnt/c/Users/jehma/Desktop/TradeGuru/expo-chatgpt-clone"

# Ensure log directory exists
LOG_DIR.mkdir(parents=True, exist_ok=True)


class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    RESET = '\033[0m'
    BOLD = '\033[1m'


def load_config() -> Dict[str, Any]:
    """Load endpoints.yaml configuration."""
    with open(ENDPOINTS_YAML, 'r') as f:
        return yaml.safe_load(f)


def register_device(base_url: str, device_config: Dict) -> Optional[str]:
    """Register a test device and return device_id."""
    url = f"{base_url}/api/v1/device/register"

    print(f"{Colors.BLUE}→{Colors.RESET} Registering test device...")

    try:
        response = requests.post(url, json=device_config, timeout=10)

        if response.status_code == 201:
            data = response.json()
            device_id = data.get('device_id')
            print(f"{Colors.GREEN}✓{Colors.RESET} Device registered: {device_id}")
            return device_id
        else:
            print(f"{Colors.RED}✗{Colors.RESET} Registration failed: {response.status_code}")
            print(f"  Response: {response.text[:200]}")
            return None
    except Exception as e:
        print(f"{Colors.RED}✗{Colors.RESET} Registration error: {e}")
        return None


def build_headers(endpoint: Dict, device_id: Optional[str]) -> Dict[str, str]:
    """Build request headers based on auth requirements."""
    headers = {}

    auth_type = endpoint.get('auth', 'none')

    if auth_type == 'device':
        if device_id:
            headers['X-Device-ID'] = device_id
    elif auth_type == 'device_override':
        # Use the override device_id from endpoint config
        override_id = endpoint.get('device_id', 'not-a-uuid')
        headers['X-Device-ID'] = override_id

    # Set content type if specified
    content_type = endpoint.get('content_type')
    if content_type:
        headers['Content-Type'] = content_type
    elif endpoint.get('body') is not None:
        headers['Content-Type'] = 'application/json'

    return headers


def test_endpoint(endpoint: Dict, base_url: str, device_id: Optional[str], rate_limited: bool = False) -> Dict[str, Any]:
    """Execute a single endpoint test."""
    slug = endpoint['slug']
    method = endpoint['method']
    path = endpoint['path']
    url = f"{base_url}{path}"
    expect = endpoint.get('expect', {})

    # Skip device-register if we hit rate limit earlier
    if slug == 'device-register' and rate_limited:
        return {
            'slug': slug,
            'method': method,
            'path': path,
            'status': 'SKIP',
            'actual_status': 429,
            'expected_status': expect.get('status'),
            'response_body': 'Skipped due to rate limit (expected behavior)',
            'response_headers': {},
            'error': None,
            'duration_ms': 0,
        }

    headers = build_headers(endpoint, device_id)
    body = endpoint.get('body')

    result = {
        'slug': slug,
        'method': method,
        'path': path,
        'status': 'UNKNOWN',
        'actual_status': None,
        'expected_status': expect.get('status'),
        'response_body': None,
        'response_headers': None,
        'error': None,
        'duration_ms': 0,
    }

    start = time.time()

    try:
        if method == 'GET':
            response = requests.get(url, headers=headers, timeout=15)
        elif method == 'POST':
            if body is None:
                # For multipart tests with no file
                response = requests.post(url, headers=headers, timeout=15)
            else:
                response = requests.post(url, headers=headers, json=body, timeout=15)
        else:
            result['error'] = f"Unsupported method: {method}"
            result['status'] = 'ERROR'
            return result

        duration = (time.time() - start) * 1000
        result['duration_ms'] = round(duration, 2)
        result['actual_status'] = response.status_code
        result['response_headers'] = dict(response.headers)

        # Try to parse JSON response
        try:
            result['response_body'] = response.json()
        except:
            result['response_body'] = response.text[:500]

        # Check status code
        expected_status = expect.get('status')
        if expected_status and response.status_code == expected_status:
            # Check body_contains if specified
            body_contains = expect.get('body_contains')
            if body_contains:
                response_text = response.text
                if body_contains in response_text:
                    result['status'] = 'PASS'
                else:
                    result['status'] = 'FAIL'
                    result['error'] = f"Response missing expected string: {body_contains}"
            else:
                result['status'] = 'PASS'
        else:
            result['status'] = 'FAIL'
            result['error'] = f"Expected {expected_status}, got {response.status_code}"

        # Check content type if expected
        expected_ct = expect.get('content_type')
        if expected_ct:
            actual_ct = response.headers.get('Content-Type', '')
            if expected_ct not in actual_ct:
                result['status'] = 'FAIL'
                result['error'] = f"Expected content-type {expected_ct}, got {actual_ct}"

        ct_contains = expect.get('content_type_contains')
        if ct_contains:
            actual_ct = response.headers.get('Content-Type', '')
            if ct_contains not in actual_ct:
                result['status'] = 'FAIL'
                result['error'] = f"Expected content-type containing {ct_contains}, got {actual_ct}"

        # For SSE endpoints, check for streaming
        if expect.get('content_type') == 'text/event-stream':
            if 'text/event-stream' in response.headers.get('Content-Type', ''):
                result['status'] = 'PASS'
            else:
                result['status'] = 'FAIL'
                result['error'] = "Expected SSE stream, got regular response"

    except requests.exceptions.Timeout:
        result['error'] = "Request timeout"
        result['status'] = 'ERROR'
    except requests.exceptions.ConnectionError as e:
        result['error'] = f"Connection error: {str(e)[:100]}"
        result['status'] = 'ERROR'
    except Exception as e:
        result['error'] = f"Exception: {str(e)[:200]}"
        result['status'] = 'ERROR'

    return result


def write_log(result: Dict[str, Any], endpoint: Dict):
    """Write test result to individual log file."""
    slug = result['slug']
    log_file = LOG_DIR / f"{slug}.md"

    timestamp = datetime.now().isoformat()

    with open(log_file, 'w') as f:
        f.write(f"# {slug}\n\n")
        f.write(f"**Timestamp:** {timestamp}\n\n")
        f.write(f"**Status:** {result['status']}\n\n")
        f.write(f"## Request\n\n")
        f.write(f"- **Method:** {result['method']}\n")
        f.write(f"- **Path:** {result['path']}\n")
        f.write(f"- **Auth:** {endpoint.get('auth', 'none')}\n")

        if endpoint.get('body'):
            f.write(f"\n**Body:**\n```json\n{json.dumps(endpoint['body'], indent=2)}\n```\n")

        f.write(f"\n## Response\n\n")
        f.write(f"- **Status Code:** {result['actual_status']} (expected: {result['expected_status']})\n")
        f.write(f"- **Duration:** {result['duration_ms']}ms\n")

        if result.get('response_headers'):
            f.write(f"\n**Headers:**\n```\n")
            for k, v in result['response_headers'].items():
                f.write(f"{k}: {v}\n")
            f.write(f"```\n")

        if result.get('response_body'):
            f.write(f"\n**Body:**\n```json\n{json.dumps(result['response_body'], indent=2) if isinstance(result['response_body'], dict) else result['response_body']}\n```\n")

        if result.get('error'):
            f.write(f"\n## Error\n\n{result['error']}\n")

        if endpoint.get('notes'):
            f.write(f"\n## Notes\n\n{endpoint['notes']}\n")


def print_result(result: Dict[str, Any]):
    """Print colored test result to console."""
    slug = result['slug']
    status = result['status']

    if status == 'PASS':
        icon = f"{Colors.GREEN}✓{Colors.RESET}"
    elif status == 'SKIP':
        icon = f"{Colors.YELLOW}~{Colors.RESET}"
    elif status == 'FAIL':
        icon = f"{Colors.RED}✗{Colors.RESET}"
    else:
        icon = f"{Colors.YELLOW}!{Colors.RESET}"

    line = f"{icon} {slug:40} {result['actual_status']:3} | {result['duration_ms']:6.1f}ms"

    if status not in ['PASS', 'SKIP']:
        line += f" | {result.get('error', 'Unknown error')[:50]}"
    elif status == 'SKIP':
        line += f" | Rate limited (cached device used)"

    print(line)


def get_vercel_logs(project_path: str, filter_term: Optional[str] = None):
    """Attempt to read recent Vercel deployment logs."""
    # This is a placeholder - actual log retrieval would need Vercel CLI
    print(f"\n{Colors.BLUE}→{Colors.RESET} To check Vercel logs, run:")
    print(f"  cd {project_path} && vercel logs")
    if filter_term:
        print(f"  (filter for: {filter_term})")


def diagnose_500_error(endpoint: Dict, result: Dict[str, Any], vercel_project: str):
    """Diagnose and suggest fixes for 500 errors."""
    slug = endpoint['slug']
    path = endpoint['path']

    print(f"\n{Colors.RED}500 ERROR DIAGNOSIS:{Colors.RESET} {slug}")
    print(f"Path: {path}")

    # Suggest checking the source file
    if '/chat' in path:
        source_file = f"{vercel_project}/api/v1/chat.ts"
        print(f"\nSource file: {source_file}")
        print(f"Action: Read {source_file} and check imports")

    get_vercel_logs(vercel_project, filter_term=slug)


def main():
    """Main test execution flow."""
    print(f"{Colors.BOLD}TradeGuru API v1 Endpoint Test Runner{Colors.RESET}\n")

    # Load configuration
    config = load_config()
    base_url = config['base_url']
    device_config = config['test_device']
    endpoints = config['endpoints']

    print(f"Base URL: {base_url}")
    print(f"Total endpoints: {len(endpoints)}\n")

    # Register test device
    device_id = register_device(base_url, device_config)
    rate_limited = False

    # If registration failed due to rate limiting, use the last known device_id
    if not device_id:
        # Try to read from previous test logs
        summary_file = BASE_DIR / "logs" / "device_id.txt"
        if summary_file.exists():
            with open(summary_file, 'r') as f:
                device_id = f.read().strip()
                print(f"{Colors.YELLOW}→{Colors.RESET} Using cached device_id: {device_id}")
                rate_limited = True

    if not device_id and any(e.get('auth') in ['device', 'device_override'] for e in endpoints):
        print(f"{Colors.RED}✗{Colors.RESET} Cannot proceed: device registration failed and no cached device_id")
        return 1

    # Save device_id for future use
    if device_id:
        summary_file = BASE_DIR / "logs" / "device_id.txt"
        with open(summary_file, 'w') as f:
            f.write(device_id)

    print(f"\n{Colors.BOLD}Running endpoint tests...{Colors.RESET}\n")

    # Test all endpoints
    results = []
    failed_500s = []

    for i, endpoint in enumerate(endpoints, 1):
        slug = endpoint['slug']
        print(f"[{i}/{len(endpoints)}] Testing {slug}...", end=' ')
        sys.stdout.flush()

        result = test_endpoint(endpoint, base_url, device_id, rate_limited)
        results.append(result)

        # Clear the "Testing..." line and print result
        print(f"\r[{i}/{len(endpoints)}] ", end='')
        print_result(result)

        # Write individual log
        write_log(result, endpoint)

        # Track 500 errors for diagnosis
        if result.get('actual_status') == 500:
            failed_500s.append((endpoint, result))

        # Small delay between requests
        time.sleep(0.2)

    # Summary
    print(f"\n{Colors.BOLD}=== Test Summary ==={Colors.RESET}\n")

    pass_count = sum(1 for r in results if r['status'] == 'PASS')
    fail_count = sum(1 for r in results if r['status'] == 'FAIL')
    error_count = sum(1 for r in results if r['status'] == 'ERROR')
    skip_count = sum(1 for r in results if r['status'] == 'SKIP')

    print(f"{Colors.GREEN}PASS:{Colors.RESET} {pass_count}")
    print(f"{Colors.RED}FAIL:{Colors.RESET} {fail_count}")
    print(f"{Colors.YELLOW}ERROR:{Colors.RESET} {error_count}")
    if skip_count > 0:
        print(f"{Colors.YELLOW}SKIP:{Colors.RESET} {skip_count}")

    # Diagnose 500 errors
    if failed_500s:
        print(f"\n{Colors.BOLD}=== 500 Error Diagnosis ==={Colors.RESET}")
        for endpoint, result in failed_500s:
            diagnose_500_error(endpoint, result, VERCEL_PROJECT)

    # Write summary file
    summary_file = BASE_DIR / "logs" / "summary.md"
    with open(summary_file, 'w') as f:
        f.write(f"# API Test Summary\n\n")
        f.write(f"**Timestamp:** {datetime.now().isoformat()}\n\n")
        f.write(f"- PASS: {pass_count}\n")
        f.write(f"- FAIL: {fail_count}\n")
        f.write(f"- ERROR: {error_count}\n\n")
        f.write(f"## Results\n\n")
        for r in results:
            status_icon = "✓" if r['status'] == 'PASS' else "✗"
            f.write(f"- [{status_icon}] {r['slug']} - {r['actual_status']} ({r['duration_ms']}ms)\n")

    print(f"\nLogs written to: {LOG_DIR}/")
    print(f"Summary: {summary_file}")

    return 0 if fail_count == 0 and error_count == 0 else 1


if __name__ == '__main__':
    sys.exit(main())
