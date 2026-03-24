#!/usr/bin/env python3
"""O&P Run Evaluator — scores completed runs against the grading framework.

4 Gates (binary pass/fail):
  - schema_compliance: every step file validates against JSON Schema
  - spec_adherence: all done_definition items met
  - no_regressions: build passes, tests pass, scope respected
  - audit_complete: all mandatory steps materialized

1 Primary Metric:
  - run_quality = spec_compliance_score * 0.4 + regression_score * 0.3 + acceptance_score * 0.3

3 Learning Metrics (tracked over time):
  - profile_accuracy: runs with no escalation / total runs
  - net_improvement_rate: (gains - regressions) / iterations
  - reliability: 1 - stddev(run_quality across similar tasks)

Usage:
  python3 evaluate.py <run_dir>                    # Score a single run
  python3 evaluate.py <run_dir> --log              # Score and append to metrics.jsonl
  python3 evaluate.py --aggregate <metrics_file>   # Show learning metrics over time
"""

import json
import sys
import os
import hashlib
import math
from pathlib import Path
from datetime import datetime


def load_json(path):
    with open(path) as f:
        return json.load(f)


def validate_checksum(file_path, expected_checksum):
    """Verify sha256 checksum of a file."""
    if not expected_checksum.startswith("sha256:"):
        return False
    expected_hash = expected_checksum[7:]
    sha = hashlib.sha256()
    with open(file_path, "rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            sha.update(chunk)
    return sha.hexdigest() == expected_hash


def check_gates(run_dir):
    """Check 4 binary gates. Returns dict of gate results."""
    run_path = Path(run_dir)
    gates = {
        "schema_compliance": True,
        "spec_adherence": True,
        "no_regressions": True,
        "audit_complete": True,
        "gate_failures": []
    }

    # Gate 1: Schema compliance — check all step files exist and have required fields
    required_base_fields = [
        "schema_version", "run_id", "step_id", "step_name", "status",
        "input_file", "input_checksum", "actor", "objective", "payload",
        "artifacts", "metrics", "hil", "decision", "trace"
    ]

    step_files = sorted(run_path.glob("*.json"))
    if not step_files:
        gates["schema_compliance"] = False
        gates["gate_failures"].append("No step files found in run directory")
        return gates

    for sf in step_files:
        try:
            data = load_json(sf)
            missing = [f for f in required_base_fields if f not in data]
            if missing:
                gates["schema_compliance"] = False
                gates["gate_failures"].append(f"{sf.name}: missing fields {missing}")
        except (json.JSONDecodeError, OSError) as e:
            gates["schema_compliance"] = False
            gates["gate_failures"].append(f"{sf.name}: parse error: {e}")

    # Gate 2: Spec adherence — check done_definition items in 01_intake vs 06_review
    intake_path = run_path / "01_intake.json"
    review_path = run_path / "06_review.json"

    if intake_path.exists() and review_path.exists():
        intake = load_json(intake_path)
        review = load_json(review_path)

        done_items = intake.get("payload", {}).get("done_definition", [])
        quality = review.get("payload", {}).get("quality", {})
        spec = quality.get("spec_compliance", {})

        if done_items and spec:
            if spec.get("items_met", 0) < spec.get("items_defined", len(done_items)):
                gates["spec_adherence"] = False
                failures = spec.get("failures", [])
                gates["gate_failures"].append(f"Spec failures: {failures}")
        elif done_items and not spec:
            gates["spec_adherence"] = False
            gates["gate_failures"].append("Review missing quality.spec_compliance section")

    # Gate 3: No regressions — check review regression section
    if review_path.exists():
        review = load_json(review_path)
        regression = review.get("payload", {}).get("quality", {}).get("regression", {})
        if regression:
            if not regression.get("build_passes", False):
                gates["no_regressions"] = False
                gates["gate_failures"].append("Build does not pass")
            if not regression.get("tests_pass", False):
                gates["no_regressions"] = False
                gates["gate_failures"].append("Tests do not pass")
            if regression.get("files_outside_scope_changed", []):
                gates["no_regressions"] = False
                gates["gate_failures"].append(f"Files outside scope changed: {regression['files_outside_scope_changed']}")

    # Gate 4: Audit completeness — check workflow resolution materialized steps exist
    resolution_path = run_path / "01a_workflow_resolution.json"
    if resolution_path.exists():
        resolution = load_json(resolution_path)
        materialized = resolution.get("payload", {}).get("materialized_steps", [])
        for step_file in materialized:
            if not (run_path / step_file).exists():
                gates["audit_complete"] = False
                gates["gate_failures"].append(f"Materialized step missing: {step_file}")

    return gates


def score_run(run_dir):
    """Score a single run. Returns gates + primary metric + operational metrics."""
    run_path = Path(run_dir)
    result = {
        "run_id": "",
        "scored_at": datetime.now().isoformat(),
        "gates": check_gates(run_dir),
        "gates_passed": False,
        "run_quality": 0.0,
        "components": {
            "spec_compliance_score": 0.0,
            "regression_score": 0.0,
            "acceptance_score": 0.0
        },
        "operational": {
            "cycle_time_sec": 0,
            "tokens_total": 0,
            "cost_usd": 0.0,
            "human_override_count": 0
        },
        "routing": {
            "predicted_profile": "",
            "escalated": False,
            "escalation_count": 0
        }
    }

    # Extract run_id
    request_path = run_path / "00_request.json"
    if request_path.exists():
        req = load_json(request_path)
        result["run_id"] = req.get("run_id", run_path.name)

    # Check if gates passed
    result["gates_passed"] = all([
        result["gates"]["schema_compliance"],
        result["gates"]["spec_adherence"],
        result["gates"]["no_regressions"],
        result["gates"]["audit_complete"]
    ])

    # Primary metric components
    review_path = run_path / "06_review.json"
    if review_path.exists():
        review = load_json(review_path)
        quality = review.get("payload", {}).get("quality", {})

        # Spec compliance score
        spec = quality.get("spec_compliance", {})
        result["components"]["spec_compliance_score"] = spec.get("score", 0.0)

        # Regression score
        reg = quality.get("regression", {})
        result["components"]["regression_score"] = reg.get("score", 0.0)

    # Acceptance score defaults to 0 until human follow-up (scored retroactively)
    result["components"]["acceptance_score"] = 0.5  # neutral default

    # Calculate primary metric
    c = result["components"]
    result["run_quality"] = (
        c["spec_compliance_score"] * 0.4 +
        c["regression_score"] * 0.3 +
        c["acceptance_score"] * 0.3
    )

    # Operational metrics from execute step
    execute_path = run_path / "05_execute.json"
    if execute_path.exists():
        exe = load_json(execute_path)
        m = exe.get("metrics", {})
        result["operational"]["cycle_time_sec"] = m.get("execution_time_sec", 0)
        result["operational"]["cost_usd"] = m.get("api_cost_usd", 0.0)

    # Routing accuracy from workflow resolution
    resolution_path = run_path / "01a_workflow_resolution.json"
    if resolution_path.exists():
        res = load_json(resolution_path)
        payload = res.get("payload", {})
        result["routing"]["predicted_profile"] = payload.get("execution_profile", "")

    # Check for escalation (existence of v2 resolution file)
    if (run_path / "01a_workflow_resolution_v2.json").exists():
        result["routing"]["escalated"] = True
        result["routing"]["escalation_count"] = 1

    return result


def aggregate_metrics(metrics_path):
    """Compute learning metrics from historical run scores."""
    metrics_file = Path(metrics_path)
    if not metrics_file.exists():
        print("No metrics file found")
        return

    runs = []
    with open(metrics_file) as f:
        for line in f:
            line = line.strip()
            if line:
                runs.append(json.loads(line))

    if not runs:
        print("No runs scored yet")
        return

    total = len(runs)
    escalated = sum(1 for r in runs if r.get("routing", {}).get("escalated", False))
    qualities = [r.get("run_quality", 0) for r in runs]

    # Profile accuracy
    profile_accuracy = (total - escalated) / max(total, 1)

    # Net improvement rate (compare first half vs second half)
    mid = total // 2
    if mid > 0:
        first_half_avg = sum(qualities[:mid]) / mid
        second_half_avg = sum(qualities[mid:]) / max(len(qualities[mid:]), 1)
        net_improvement = second_half_avg - first_half_avg
    else:
        net_improvement = 0.0

    # Reliability (1 - stddev of quality scores)
    if len(qualities) > 1:
        mean_q = sum(qualities) / len(qualities)
        variance = sum((q - mean_q) ** 2 for q in qualities) / (len(qualities) - 1)
        stddev = math.sqrt(variance)
        reliability = max(0, 1 - stddev)
    else:
        reliability = 1.0

    print(f"O&P Learning Metrics ({total} runs)")
    print(f"{'=' * 40}")
    print(f"  profile_accuracy:     {profile_accuracy:.4f}")
    print(f"  net_improvement_rate: {net_improvement:+.4f}")
    print(f"  reliability:          {reliability:.4f}")
    print(f"  avg_run_quality:      {sum(qualities) / total:.4f}")
    print(f"  total_escalations:    {escalated}")
    print(f"  gates_failed:         {sum(1 for r in runs if not r.get('gates_passed', False))}")

    return {
        "profile_accuracy": round(profile_accuracy, 4),
        "net_improvement_rate": round(net_improvement, 4),
        "reliability": round(reliability, 4),
        "avg_run_quality": round(sum(qualities) / total, 4),
        "total_runs": total,
        "total_escalations": escalated
    }


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage:")
        print("  python3 evaluate.py <run_dir>              # Score a run")
        print("  python3 evaluate.py <run_dir> --log        # Score and log")
        print("  python3 evaluate.py --aggregate <metrics>  # Learning metrics")
        sys.exit(1)

    if sys.argv[1] == "--aggregate":
        metrics_path = sys.argv[2] if len(sys.argv) > 2 else "autoresearch/oandp/metrics.jsonl"
        aggregate_metrics(metrics_path)
    else:
        run_dir = sys.argv[1]
        log = "--log" in sys.argv

        scores = score_run(run_dir)

        print(f"O&P Run Evaluation: {scores['run_id']}")
        print(f"{'=' * 40}")
        print(f"  Gates passed:     {'YES' if scores['gates_passed'] else 'NO'}")
        for gate, passed in scores['gates'].items():
            if gate == "gate_failures":
                continue
            print(f"    {gate}: {'PASS' if passed else 'FAIL'}")
        if scores['gates']['gate_failures']:
            print(f"  Gate failures:")
            for f in scores['gates']['gate_failures']:
                print(f"    - {f}")
        print(f"  Run quality:      {scores['run_quality']:.4f}")
        print(f"    spec_compliance: {scores['components']['spec_compliance_score']:.2f}")
        print(f"    regression:      {scores['components']['regression_score']:.2f}")
        print(f"    acceptance:      {scores['components']['acceptance_score']:.2f}")
        print(f"  Profile:          {scores['routing']['predicted_profile']}")
        print(f"  Escalated:        {scores['routing']['escalated']}")

        if log:
            metrics_file = os.environ.get("OANDP_METRICS", "autoresearch/oandp/metrics.jsonl")
            with open(metrics_file, "a") as f:
                f.write(json.dumps(scores) + "\n")
            print(f"\n  Logged to {metrics_file}")
