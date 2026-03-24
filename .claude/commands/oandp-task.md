---
description: "Submit a task to the O&P workflow. Creates run, produces all mandatory files, executes, reviews, scores. Usage: /oandp-task <describe the task>"
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep", "Bash", "Agent"]
---

<task>
Execute a task through the O&P file-contract workflow. Every run produces a complete audit trail of JSON manifests, with quality measurement and self-improvement scoring.
</task>

<arguments>
The user specified: `$ARGUMENTS`

If empty, ask: "What task should I run through the O&P pipeline?"
</arguments>

<constraints>
## Completion Criteria

The run is valid when:
- All 5 mandatory files exist: `00_request.json`, `01_intake.json`, `01a_workflow_resolution.json`, `05_execute.json`, `06_review.json`
- Each step file passes `runner.py validate`
- `06_review.json` contains `quality.spec_compliance` and `quality.regression`
- `01a_workflow_resolution.json` documents all skipped steps with reasons
- `runner.py check` reports ALL PASS
- `evaluate.py --log` has scored the run

## Constraint Hierarchy (highest priority first)

**PRESERVE (always maintain):**
- Every run begins with `runner.py new`
- Every step reads one input file and writes one output file
- The two-agent gate produces independent assessments
- Validation runs between every step

**REQUIRE (every run):**
- 5 mandatory manifests produced in sequence
- Quality measurement in 06_review (spec_compliance + regression)
- Post-run scoring via evaluate.py
- Post-run categorization comparing predicted vs actual

**CONDITIONAL (profile-dependent):**
- Planning steps match the materialized path from 01a
- HIL gates activate for HIGH_RISK profile
- 07_integrate, 08_improve, 09_promote materialize per profile
</constraints>

<workflow>
## Step 1: Initialize Run

```bash
python3 autoresearch/oandp/runner.py new "$ARGUMENTS"
```

Capture the run directory path. All subsequent files write there.

## Step 2: Produce 00_request.json

Normalize the user's request into structured JSON.

Payload fields: `request_text`, `goal`, `constraints`, `desired_outputs`, `priority`, `task_class`.

Validate:
```bash
python3 autoresearch/oandp/runner.py validate <run_dir> 00_request.json
```

## Step 3: Produce 01_intake.json

Define scope and success criteria.

Payload fields: `problem_statement`, `scope_in`, `scope_out`, `success_metrics`, `constraints`, `done_definition`, `risk_level`.

The `done_definition` contains concrete, checkable items. These become the spec_compliance checklist in step 06.

Validate:
```bash
python3 autoresearch/oandp/runner.py validate <run_dir> 01_intake.json
```

## Step 4: Produce 01a_workflow_resolution.json (Two-Agent Gate)

Read expertise first:
```bash
cat autoresearch/oandp/expertise/routing_rules.jsonl
```

Spawn **Agent A** (model: haiku):

<agent_a_prompt>
Read <run_dir>/00_request.json and <run_dir>/01_intake.json.

The assessment covers 6 routing drivers:
- task_class: build, bug_fix, feature, research, ops, content, doc_update, config_tweak, integration, deployment, other
- estimated_complexity: low, medium, high
- risk_level: low, medium, high
- reversibility: easy, moderate, hard
- production_impact: true/false
- compliance_impact: true/false

Apply this selection logic (first match):
1. risk_level == high → HIGH_RISK
2. estimated_complexity == high → COMPLEX
3. task_class in [bug_fix, doc_update, config_tweak] AND complexity == low AND risk == low AND reversibility == easy AND production_impact == false AND compliance_impact == false → TRIVIAL
4. Otherwise → STANDARD

Output JSON with: proposed_profile, confidence (0-1), reasoning, materialized_steps, skipped_steps, skip_reasons.
</agent_a_prompt>

Spawn **Agent B** (model: haiku):

<agent_b_prompt>
Read <run_dir>/00_request.json and <run_dir>/01_intake.json.

Independently assess the 6 routing drivers (task_class, estimated_complexity, risk_level, reversibility, production_impact, compliance_impact).

Apply the profile selection logic:
1. risk_level == high → HIGH_RISK
2. estimated_complexity == high → COMPLEX
3. Trivial criteria met → TRIVIAL
4. Otherwise → STANDARD

Output JSON with: proposed_profile, confidence (0-1), reasoning.

This assessment is independent. Base it only on the request and intake files.
</agent_b_prompt>

Compare results:
- Agreement on profile → write `01a_workflow_resolution.json` with both assessments
- Disagreement → set `decision.result = "needs_human"`, stop, ask the user

Include `materialized_steps`, `skipped_steps`, `skip_reasons` in the payload.

Validate:
```bash
python3 autoresearch/oandp/runner.py validate <run_dir> 01a_workflow_resolution.json
```

## Step 5: Produce Planning Steps

Read `01a_workflow_resolution.json` for the materialized path.

| Profile | Planning Files |
|---------|---------------|
| TRIVIAL | `02_03_04_plan.json` (collapsed) |
| STANDARD | `02_decompose.json`, `03_system_select.json`, `04_design.json` |
| COMPLEX | `02_decompose.json`, `03_system_select.json`, `04_design.json` |
| HIGH_RISK | `02_decompose.json`, `03_system_select.json`, `04_design.json` (with HIL gates) |

Validate each file after writing.

## Step 6: Produce 05_execute.json

Execute the work. Spawn worker agents appropriate to the task:

| Task Type | Agent | Model |
|-----------|-------|-------|
| New code files | code-scribe | sonnet |
| Edit existing files | code-surgeon | sonnet |
| Research/analysis | planner | sonnet |
| Quick assessment | quick-thinker | haiku |

The execute manifest references every file changed in `change_manifest` and every artifact in `artifact_manifest`.

Validate:
```bash
python3 autoresearch/oandp/runner.py validate <run_dir> 05_execute.json
```

## Step 7: Produce 06_review.json (Quality Gate)

Review execution against `done_definition` from step 01.

The payload includes this quality section:

```json
"quality": {
  "spec_compliance": {
    "items_defined": 0,
    "items_met": 0,
    "score": 0.0,
    "failures": []
  },
  "regression": {
    "build_passes": true,
    "tests_pass": true,
    "new_warnings": 0,
    "files_outside_scope_changed": [],
    "score": 1.0
  }
}
```

Decision result mapping:
- `spec_compliance.score == 1.0` AND `regression.score == 1.0` → `pass`
- Fixable issues found → `needs_revision` (route back to step 05 or 04)
- Fundamental problems → `fail`

Validate:
```bash
python3 autoresearch/oandp/runner.py validate <run_dir> 06_review.json
```

## Step 8: Produce Remaining Steps

Based on the materialized path from 01a, produce any of:
- `07_integrate.json` — assemble release candidate
- `08_improve.json` — capture what_worked, what_failed, bottlenecks
- `09_promote.json` — deploy and verify

## Step 9: Validate and Score

```bash
python3 autoresearch/oandp/runner.py check <run_dir>
python3 autoresearch/oandp/evaluate.py <run_dir> --log
```

Print check results and score to the user.

## Step 10: Categorize

Append to `autoresearch/oandp/run_categorizations.jsonl`:

```json
{
  "run_id": "...",
  "predicted": {
    "task_class": "...",
    "complexity": "...",
    "risk": "...",
    "profile": "..."
  },
  "actual": {
    "task_class": "...",
    "complexity": "...",
    "risk": "...",
    "profile_needed": "...",
    "escalated": false,
    "revision_loops": 0,
    "human_overrides": 0
  },
  "lesson": "...",
  "applies_to": "..."
}
```

When predicted differs from actual, write a new routing rule to `autoresearch/oandp/expertise/routing_rules.jsonl`.
</workflow>

<recovery>
## When Validation Fails

1. Read the error message from `runner.py validate`
2. Fix the specific field or structure cited
3. Re-validate
4. Continue the sequence

## When Review Returns needs_revision

1. Read `06_review.json` decision.reason
2. Route back to step 04 (design) or step 05 (execute) as specified
3. Produce a revised manifest
4. Re-run step 06

## When Agents Disagree at Step 4

1. Present both assessments to the user
2. Ask which profile to use
3. Write `01a_workflow_resolution.json` with the human decision recorded in `hil.approved_by`
</recovery>
