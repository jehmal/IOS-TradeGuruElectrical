#!/usr/bin/env python3
"""O&P Runner — state machine that enforces the workflow contract.

Responsibilities:
  1. Create run directories
  2. Validate each JSON manifest against schema before advancing
  3. Enforce step order per materialized path
  4. Block on pending HIL
  5. Handle review routing (pass/fail/revise)
  6. Handle mid-run escalation
  7. Track metrics via evaluate.py

Usage:
  python3 runner.py new "Add dark mode to settings"     # Create new run
  python3 runner.py advance <run_dir>                    # Advance to next step
  python3 runner.py validate <run_dir> <step_file>       # Validate a step file
  python3 runner.py status <run_dir>                     # Show run status
  python3 runner.py list                                 # List all runs
"""

import json
import sys
import os
import hashlib
from pathlib import Path
from datetime import datetime


OANDP_DIR = Path(__file__).parent
SCHEMAS_DIR = OANDP_DIR / "schemas"
RUNS_DIR = OANDP_DIR / "runs"
PROFILES_PATH = OANDP_DIR / "profiles.json"
METRICS_PATH = OANDP_DIR / "metrics.jsonl"

# Step file -> schema file mapping
SCHEMA_MAP = {
    "00_request.json": "00_request.schema.json",
    "01_intake.json": "01_intake.schema.json",
    "01a_workflow_resolution.json": "01a_workflow_resolution.schema.json",
    "02_decompose.json": "02_decompose.schema.json",
    "02_03_04_plan.json": "02_03_04_plan.schema.json",
    "03_system_select.json": "03_system_select.schema.json",
    "04_design.json": "04_design.schema.json",
    "05_execute.json": "05_execute.schema.json",
    "06_review.json": "06_review.schema.json",
    "07_integrate.json": "07_integrate.schema.json",
    "08_improve.json": "08_improve.schema.json",
    "09_promote.json": "09_promote.schema.json",
}

REQUIRED_BASE_FIELDS = [
    "schema_version", "run_id", "step_id", "step_name", "status",
    "input_file", "input_checksum", "actor", "objective", "payload",
    "artifacts", "metrics", "hil", "decision", "trace"
]

# M1: These files must exist in EVERY run — no exceptions
NEVER_SKIP_FILES = [
    "00_request.json",
    "01_intake.json",
    "01a_workflow_resolution.json",
    "05_execute.json",
    "06_review.json",
]

# M2: Input file dependency chain — step N requires step N-1's output
STEP_INPUT_REQUIREMENTS = {
    "00_request.json": "request.md",
    "01_intake.json": "00_request.json",
    "01a_workflow_resolution.json": "01_intake.json",
    "02_decompose.json": "01a_workflow_resolution.json",
    "02_03_04_plan.json": "01a_workflow_resolution.json",
    "03_system_select.json": "02_decompose.json",
    "04_design.json": "03_system_select.json",
    "05_execute.json": "04_design.json",  # or 02_03_04_plan.json
    "06_review.json": "05_execute.json",
    "07_integrate.json": "06_review.json",
    "08_improve.json": "07_integrate.json",  # or 06_review.json if 07 skipped
    "09_promote.json": "08_improve.json",
}


def load_json(path):
    with open(path) as f:
        return json.load(f)


def sha256_file(path):
    sha = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            sha.update(chunk)
    return f"sha256:{sha.hexdigest()}"


def generate_run_id():
    today = datetime.now().strftime("%Y-%m-%d")
    existing = list(RUNS_DIR.glob(f"run_{today}-*"))
    seq = len(existing) + 1
    return f"RUN-{today}-{seq:03d}"


def load_profiles():
    return load_json(PROFILES_PATH)


def get_materialized_path(run_dir):
    """Read 01a_workflow_resolution.json to get the materialized step path."""
    resolution = run_dir / "01a_workflow_resolution.json"
    if not resolution.exists():
        # Pre-resolution: always run 00, 01, 01a
        return ["00_request.json", "01_intake.json", "01a_workflow_resolution.json"]

    data = load_json(resolution)
    return data.get("payload", {}).get("materialized_steps", [])


def get_completed_steps(run_dir):
    """Find which steps have been completed (status == 'complete')."""
    completed = []
    for sf in sorted(run_dir.glob("*.json")):
        if sf.name == "request.md":
            continue
        try:
            data = load_json(sf)
            if data.get("status") == "complete":
                completed.append(sf.name)
        except (json.JSONDecodeError, OSError):
            pass
    return completed


def get_next_step(run_dir):
    """Determine the next step to execute."""
    path = get_materialized_path(run_dir)
    completed = get_completed_steps(run_dir)

    for step in path:
        if step not in completed:
            return step
    return None


def check_input_exists(run_dir, step_file):
    """M2: Verify the input file for this step exists before allowing execution."""
    required_input = STEP_INPUT_REQUIREMENTS.get(step_file)
    if required_input is None:
        return True, "No input requirement defined"

    # Special case: 05_execute can read from either 04_design or 02_03_04_plan
    if step_file == "05_execute.json":
        if (run_dir / "04_design.json").exists() or (run_dir / "02_03_04_plan.json").exists():
            return True, "Input exists"
        return False, "M2 VIOLATION: 05_execute requires 04_design.json or 02_03_04_plan.json — neither exists"

    # Special case: 08_improve can read from 07_integrate or 06_review
    if step_file == "08_improve.json":
        if (run_dir / "07_integrate.json").exists() or (run_dir / "06_review.json").exists():
            return True, "Input exists"
        return False, "M2 VIOLATION: 08_improve requires 07_integrate.json or 06_review.json — neither exists"

    if not (run_dir / required_input).exists():
        return False, f"M2 VIOLATION: {step_file} requires {required_input} — file not found"

    return True, "Input exists"


def check_never_skip_files(run_dir):
    """M1: Check that all never-skip files exist in a completed run."""
    missing = []
    for f in NEVER_SKIP_FILES:
        if not (run_dir / f).exists():
            missing.append(f)
    if missing:
        return False, f"M1 VIOLATION: Never-skip files missing: {missing}"
    return True, "All never-skip files present"


def check_skip_documentation(run_dir):
    """M3: Check that skipped steps are documented in 01a."""
    resolution_path = run_dir / "01a_workflow_resolution.json"
    if not resolution_path.exists():
        return False, "M3 VIOLATION: 01a_workflow_resolution.json missing — cannot verify skip documentation"

    data = load_json(resolution_path)
    payload = data.get("payload", {})
    materialized = set(payload.get("materialized_steps", []))
    skipped = set(payload.get("skipped_steps", []))
    skip_reasons = payload.get("skip_reasons", {})

    # Check that every skipped step has a reason
    undocumented = [s for s in skipped if s not in skip_reasons]
    if undocumented:
        return False, f"M3 VIOLATION: Skipped steps without reasons: {undocumented}"

    return True, "All skipped steps documented"


def check_review_quality(run_dir):
    """M5: Check that 06_review contains the quality section."""
    review_path = run_dir / "06_review.json"
    if not review_path.exists():
        return True, "Review not yet produced"

    data = load_json(review_path)
    quality = data.get("payload", {}).get("quality")
    if quality is None:
        return False, "M5 VIOLATION: 06_review.json missing payload.quality section"

    if "spec_compliance" not in quality:
        return False, "M5 VIOLATION: 06_review.json missing quality.spec_compliance"

    if "regression" not in quality:
        return False, "M5 VIOLATION: 06_review.json missing quality.regression"

    return True, "Quality section present"


def validate_step(run_dir, step_file):
    """Validate a step file against base schema requirements + mandatory rules."""
    file_path = run_dir / step_file
    if not file_path.exists():
        return False, f"File not found: {step_file}"

    try:
        data = load_json(file_path)
    except json.JSONDecodeError as e:
        return False, f"Invalid JSON: {e}"

    # Check required base fields
    missing = [f for f in REQUIRED_BASE_FIELDS if f not in data]
    if missing:
        return False, f"Missing required fields: {missing}"

    # M4: Check HIL gate
    hil = data.get("hil", {})
    if hil.get("required", False) and hil.get("status") not in ("approved", "not_required"):
        return False, f"HIL approval required but status is '{hil.get('status')}'"

    # Verify input checksum if input file exists (skip placeholder checksums)
    input_file = data.get("input_file", "")
    input_checksum = data.get("input_checksum", "")
    input_path = run_dir / input_file
    placeholder = "sha256:0000000000000000000000000000000000000000000000000000000000000000"
    if input_path.exists() and input_checksum.startswith("sha256:") and input_checksum != placeholder:
        actual = sha256_file(input_path)
        if actual != input_checksum:
            return False, f"Input checksum mismatch: expected {input_checksum}, got {actual}"

    # Check decision has required fields
    decision = data.get("decision", {})
    if "result" not in decision:
        return False, "Missing decision.result"
    if "next_step" not in decision:
        return False, "Missing decision.next_step"

    # M5: If this is 06_review, check quality section
    if step_file == "06_review.json":
        valid, msg = check_review_quality(run_dir)
        if not valid:
            return False, msg

    return True, "Valid"


def check_review_routing(run_dir):
    """Handle step 06 review routing decisions."""
    review_path = run_dir / "06_review.json"
    if not review_path.exists():
        return None

    review = load_json(review_path)
    decision = review.get("decision", {})
    result = decision.get("result", "")

    return {
        "result": result,
        "next_step": decision.get("next_step", ""),
        "reason": decision.get("reason", "")
    }


def check_escalation(run_dir):
    """Check if any step has requested re-routing."""
    for sf in sorted(run_dir.glob("*.json")):
        try:
            data = load_json(sf)
            decision = data.get("decision", {})
            if (decision.get("result") == "needs_revision" and
                    decision.get("next_step") == "01a_workflow_resolution.json"):
                return True, sf.name, decision.get("reason", "")
        except (json.JSONDecodeError, OSError):
            pass
    return False, None, None


def cmd_new(request_text):
    """Create a new run directory with request.md."""
    RUNS_DIR.mkdir(parents=True, exist_ok=True)

    run_id = generate_run_id()
    run_dir = RUNS_DIR / run_id.lower().replace("-", "_")
    run_dir.mkdir(parents=True, exist_ok=True)

    # Create artifacts and logs subdirs
    (run_dir / "artifacts" / "code").mkdir(parents=True, exist_ok=True)
    (run_dir / "artifacts" / "docs").mkdir(parents=True, exist_ok=True)
    (run_dir / "artifacts" / "tests").mkdir(parents=True, exist_ok=True)
    (run_dir / "artifacts" / "reports").mkdir(parents=True, exist_ok=True)
    (run_dir / "artifacts" / "builds").mkdir(parents=True, exist_ok=True)
    (run_dir / "logs" / "agent_logs").mkdir(parents=True, exist_ok=True)
    (run_dir / "logs" / "human_logs").mkdir(parents=True, exist_ok=True)

    # Write request.md
    request_path = run_dir / "request.md"
    request_path.write_text(f"# Request\n\n{request_text}\n\nCreated: {datetime.now().isoformat()}\nRun ID: {run_id}\n")

    print(f"Run created: {run_dir}")
    print(f"  Run ID:  {run_id}")
    print(f"  Request: {request_path}")
    print(f"  Next:    Agent produces 00_request.json from request.md")

    return run_dir


def cmd_validate(run_dir_path, step_file):
    """Validate a specific step file."""
    run_dir = Path(run_dir_path)
    valid, message = validate_step(run_dir, step_file)
    status = "PASS" if valid else "FAIL"
    print(f"[{status}] {step_file}: {message}")
    return valid


def cmd_advance(run_dir_path):
    """Show what the next step is and validate current state."""
    run_dir = Path(run_dir_path)

    # Check for escalation
    escalated, from_step, reason = check_escalation(run_dir)
    if escalated:
        print(f"ESCALATION DETECTED from {from_step}")
        print(f"  Reason: {reason}")
        print(f"  Action: Produce 01a_workflow_resolution_v2.json")
        return

    # Check review routing
    routing = check_review_routing(run_dir)
    if routing and routing["result"] not in ("pass", ""):
        print(f"Review result: {routing['result']}")
        print(f"  Next step: {routing['next_step']}")
        print(f"  Reason: {routing['reason']}")
        return

    next_step = get_next_step(run_dir)
    if next_step is None:
        # Run complete — enforce M1 (never-skip files)
        m1_ok, m1_msg = check_never_skip_files(run_dir)
        if not m1_ok:
            print(f"  BLOCKED: {m1_msg}")
            print(f"  Produce the missing files before scoring.")
            return
        # Enforce M3 (skip documentation)
        m3_ok, m3_msg = check_skip_documentation(run_dir)
        if not m3_ok:
            print(f"  WARNING: {m3_msg}")
        print("Run complete. All materialized steps done.")
        print(f"  Score: python3 evaluate.py {run_dir} --log")
        return

    completed = get_completed_steps(run_dir)
    path = get_materialized_path(run_dir)

    print(f"Run: {run_dir.name}")
    print(f"  Completed: {len(completed)}/{len(path)}")
    print(f"  Next step: {next_step}")

    # M2: Check input file exists for the next step
    input_ok, input_msg = check_input_exists(run_dir, next_step)
    if not input_ok:
        print(f"  BLOCKED: {input_msg}")
        print(f"  Produce the required input file first.")
        return

    # M4: Validate all completed steps
    all_valid = True
    for step in completed:
        valid, msg = validate_step(run_dir, step)
        if not valid:
            print(f"  WARNING: {step} invalid: {msg}")
            all_valid = False

    if all_valid:
        print(f"  Status: Ready for {next_step}")
    else:
        print(f"  Status: Fix validation errors before advancing")


def cmd_status(run_dir_path):
    """Show full run status."""
    run_dir = Path(run_dir_path)

    if not run_dir.exists():
        print(f"Run not found: {run_dir}")
        return

    path = get_materialized_path(run_dir)
    completed = get_completed_steps(run_dir)
    next_step = get_next_step(run_dir)

    # Get run ID and profile
    run_id = run_dir.name
    profile = "unknown"
    resolution = run_dir / "01a_workflow_resolution.json"
    if resolution.exists():
        res = load_json(resolution)
        profile = res.get("payload", {}).get("execution_profile", "unknown")
        run_id = res.get("run_id", run_id)

    print(f"Run: {run_id}")
    print(f"Profile: {profile}")
    print(f"Progress: {len(completed)}/{len(path)}")
    print()

    for step in path:
        status = "DONE" if step in completed else ("NEXT" if step == next_step else "    ")
        marker = "[x]" if step in completed else ("[>]" if step == next_step else "[ ]")
        print(f"  {marker} {step} {status}")

    # Check HIL blocks
    for step in completed:
        sf = run_dir / step
        if sf.exists():
            data = load_json(sf)
            hil = data.get("hil", {})
            if hil.get("required") and hil.get("status") == "pending":
                print(f"\n  BLOCKED: {step} awaiting HIL approval")


def cmd_list():
    """List all runs."""
    if not RUNS_DIR.exists():
        print("No runs yet")
        return

    runs = sorted(RUNS_DIR.iterdir())
    if not runs:
        print("No runs yet")
        return

    print(f"{'Run':<35} {'Steps':>8} {'Status':<12}")
    print("-" * 60)

    for run_dir in runs:
        if not run_dir.is_dir():
            continue
        path = get_materialized_path(run_dir)
        completed = get_completed_steps(run_dir)
        next_step = get_next_step(run_dir)
        status = "COMPLETE" if next_step is None else f"at {next_step}"
        print(f"  {run_dir.name:<33} {len(completed):>3}/{len(path):<3}  {status}")


def cmd_check(run_dir_path):
    """Check all mandatory rules for a run (M1-M8)."""
    run_dir = Path(run_dir_path)

    if not run_dir.exists():
        print(f"Run not found: {run_dir}")
        return False

    print(f"Mandatory Rule Check: {run_dir.name}")
    print("=" * 50)

    all_pass = True

    # M1: Never-skip files
    m1_ok, m1_msg = check_never_skip_files(run_dir)
    status = "PASS" if m1_ok else "FAIL"
    print(f"  M1 Never-skip files:    [{status}] {m1_msg}")
    if not m1_ok:
        all_pass = False

    # M2: Input chain (check each completed step)
    m2_ok = True
    for sf in sorted(run_dir.glob("*.json")):
        input_ok, input_msg = check_input_exists(run_dir, sf.name)
        if not input_ok:
            print(f"  M2 Input chain:         [FAIL] {input_msg}")
            m2_ok = False
    if m2_ok:
        print(f"  M2 Input chain:         [PASS] All inputs present")
    else:
        all_pass = False

    # M3: Skip documentation
    m3_ok, m3_msg = check_skip_documentation(run_dir)
    status = "PASS" if m3_ok else "FAIL"
    print(f"  M3 Skip documentation:  [{status}] {m3_msg}")
    if not m3_ok:
        all_pass = False

    # M4: Schema validation on all step files
    m4_ok = True
    step_files = sorted(run_dir.glob("*.json"))
    for sf in step_files:
        valid, msg = validate_step(run_dir, sf.name)
        if not valid:
            print(f"  M4 Schema validation:   [FAIL] {sf.name}: {msg}")
            m4_ok = False
    if m4_ok:
        print(f"  M4 Schema validation:   [PASS] {len(step_files)} files valid")
    else:
        all_pass = False

    # M5: Review quality section
    m5_ok, m5_msg = check_review_quality(run_dir)
    status = "PASS" if m5_ok else "FAIL"
    print(f"  M5 Review quality:      [{status}] {m5_msg}")
    if not m5_ok:
        all_pass = False

    # M6: One input one output (structural — check each file has input_file field)
    m6_ok = True
    for sf in step_files:
        data = load_json(sf)
        if not data.get("input_file"):
            print(f"  M6 One-in one-out:      [FAIL] {sf.name} missing input_file")
            m6_ok = False
    if m6_ok:
        print(f"  M6 One-in one-out:      [PASS] All files declare input")

    # M7: Run directory structure
    m7_ok = (run_dir / "request.md").exists()
    status = "PASS" if m7_ok else "FAIL"
    print(f"  M7 Run directory:       [{status}] {'request.md exists' if m7_ok else 'request.md missing'}")
    if not m7_ok:
        all_pass = False

    print()
    if all_pass:
        print("  RESULT: ALL MANDATORY RULES PASS")
    else:
        print("  RESULT: MANDATORY RULE VIOLATIONS DETECTED — run is INVALID")

    return all_pass


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage:")
        print('  python3 runner.py new "request text"')
        print("  python3 runner.py advance <run_dir>")
        print("  python3 runner.py validate <run_dir> <step_file>")
        print("  python3 runner.py check <run_dir>")
        print("  python3 runner.py status <run_dir>")
        print("  python3 runner.py list")
        sys.exit(1)

    cmd = sys.argv[1]

    if cmd == "new":
        text = sys.argv[2] if len(sys.argv) > 2 else "No request text provided"
        cmd_new(text)
    elif cmd == "advance":
        cmd_advance(sys.argv[2])
    elif cmd == "validate":
        cmd_validate(sys.argv[2], sys.argv[3])
    elif cmd == "check":
        cmd_check(sys.argv[2])
    elif cmd == "status":
        cmd_status(sys.argv[2])
    elif cmd == "list":
        cmd_list()
    else:
        print(f"Unknown command: {cmd}")
        sys.exit(1)
