# Operations & Process (O&P)

## Universal Agentic Workflow Standard v1.0

A file-contract driven workflow system for agents and humans. Handles everything from typo fixes to product builds with the same framework — scaling ceremony to match complexity.

---

## Core Principle

Every step has one input file, one output file, and one decision. Artifacts are referenced, never the handoff. The workflow stays measurable, auditable, and machine-executable.

---

## Part 1: Pre-Workflow Gate

Before any workflow executes, 9 questions must be answered.

### Asked

1. What do you want?
2. Why do you want it?
3. What does done look like?

### Understood

4. What exists today that this touches?
5. What could go wrong?
6. Who cares about the outcome?

### Decided

7. How much process does this deserve?
8. Who needs to approve what?
9. Go or no-go?

Everything in this system exists to answer these 9 questions and act on the answers.

---

## Part 2: The Two-Agent Gate

No single agent can be trusted to both assess a task AND decide how much scrutiny it deserves. That is a contractor deciding whether they need a building inspector.

Two roles. Not ten. Two.

```
USER REQUEST
     |
     v
+---------+
| AGENT A |  Ask, understand, propose
| (Intake)|
+----+----+
     |
     |  "Here's what they want,
     |   here's what it touches,
     |   I think it's STANDARD"
     |
     v
+---------+
| AGENT B |  Challenge, verify, validate
| (Judge) |
+----+----+
     |
     +-- AGREE ----------> EXECUTE WORKFLOW
     |
     +-- DISAGREE --------> HUMAN DECIDES
     |
     +-- UNCERTAIN -------> HUMAN DECIDES
```

### Agent A (Intake)

- Asks the 9 questions
- Structures the answers
- Assesses the 6 routing drivers
- Proposes an execution profile

### Agent B (Judge)

- Reads the same raw input independently
- Reads Agent A's assessment
- Checks whether the proposed profile makes sense
- Flags disagreements

**If they agree:** workflow proceeds.
**If they disagree:** human decides.
**If either is uncertain:** human decides.

Cost is minimal — Agent B reads two files, outputs one judgment, a few hundred tokens. But it catches the one failure that breaks the entire system: a HIGH_RISK task misclassified as TRIVIAL.

---

## Part 3: The 6 Routing Drivers

These determine how much process a task deserves.

| Driver | Question | Values |
|--------|----------|--------|
| `task_class` | What kind of work is this? | `bug_fix`, `feature`, `research`, `ops`, `content`, `integration`, `deployment`, `doc_update`, `config_tweak`, `other` |
| `estimated_complexity` | How much work is involved? | `low`, `medium`, `high` |
| `risk_level` | What's the worst case? | `low`, `medium`, `high` |
| `reversibility` | How easy to undo? | `easy`, `moderate`, `hard` |
| `production_impact` | Does this touch live systems? | `true`, `false` |
| `compliance_impact` | Legal, safety, or regulatory? | `true`, `false` |

A "bug fix" can be trivial or dangerous. `task_class` alone is never enough. The 6 drivers together capture the real picture.

---

## Part 4: Execution Profiles

### TRIVIAL

**Use when:** bug fix, doc update, config tweak, copy change. Low complexity, low risk, easily reversible. No compliance, no production impact.

**Materialized path:**
```
00 -> 01 -> 01a -> 02_03_04 (collapsed) -> 05 -> 06 -> 08
```

### STANDARD

**Use when:** normal feature work. Medium complexity, moderate dependencies, some testing needed. Low to medium risk.

**Materialized path:**
```
00 -> 01 -> 01a -> 02 -> 03 -> 04 -> 05 -> 06 -> 07 -> 08
```

Step 09 only appears if deployment is in scope.

### COMPLEX

**Use when:** high complexity, multi-agent/multi-component work, integration dependencies, external systems, longer duration.

**Materialized path:**
```
00 -> 01 -> 01a -> 02 -> 03 -> 04 -> 05 -> 06 -> 07 -> 08 -> 09
```

Everything materialized.

### HIGH_RISK

**Use when:** high production impact, safety/compliance/legal concerns, irreversible changes, customer-facing critical, financial or operational risk.

**Materialized path:**
```
00 -> 01 -> 01a -> 02 -> 03 -> 04 -> 05 -> 06 -> 07 -> 08 -> 09
```

Same as COMPLEX but with **mandatory HIL gates** at: system selection (03), design approval (04), post-review (06), and promotion (09).

### Profile Selection Logic

```
IF risk_level = high
  -> HIGH_RISK

ELSE IF estimated_complexity = high
  -> COMPLEX

ELSE IF task_class IN [bug_fix, doc_update, config_tweak]
  AND estimated_complexity = low
  AND risk_level = low
  AND reversibility = easy
  AND production_impact = false
  AND compliance_impact = false
  -> TRIVIAL

ELSE
  -> STANDARD
```

---

## Part 5: Hard Guardrails

These rules override profile selection. They are non-negotiable.

| Rule | Condition | Enforcement |
|------|-----------|-------------|
| 1 | `risk_level = high` | No collapsing beyond planning compression |
| 2 | `production_impact = true` | `06_review.json` may never be skipped |
| 3 | `compliance_impact = true` | Steps 03, 04, 06, 09 must all be materialized |
| 4 | `customer_impact = true` AND `reversibility = hard` | Mandatory HIL before execution and promotion |
| 5 | More than one artifact family changed | `07_integrate.json` required |

Artifact family examples: backend + frontend, code + infra, automation + docs, model + prompt + workflow.

---

## Part 6: Steps That Never Skip

These are the minimum viable audit trail. Without them, the workflow stops being transparent.

| Step | Why |
|------|-----|
| `00_request.json` | What was asked |
| `01_intake.json` | What it means |
| `01a_workflow_resolution.json` | Why the path was chosen |
| `05_execute.json` | What was done |
| `06_review.json` | Whether it passed |

---

## Part 7: Steps That Can Collapse

### Safe to collapse (TRIVIAL profile)

Steps 02, 03, and 04 are planning steps. For trivial work they are bundled into:

```
02_03_04_plan.json
```

This file contains all three logical sections — decomposition, system selection, and design — in one manifest. The logical steps never disappear. Only the physical files are merged.

### Safe to skip (with documented reason)

| Step | Skip when |
|------|-----------|
| `07_integrate` | Single component change, no assembly needed |
| `09_promote` | No deployment, no environment promotion, deliverable is internal |

### Never collapse, never skip

Step 06 (REVIEW) is the main gatekeeping step. It always produces a single unambiguous result.

---

## Part 8: The Canonical Workflow Steps

### Run Directory Structure

```
/run_<run_id>/
|
+-- request.md
+-- 00_request.json
+-- 01_intake.json
+-- 01a_workflow_resolution.json
+-- 02_decompose.json          (or 02_03_04_plan.json)
+-- 03_system_select.json      (or collapsed above)
+-- 04_design.json             (or collapsed above)
+-- 05_execute.json
+-- 06_review.json
+-- 07_integrate.json
+-- 08_improve.json
+-- 09_promote.json
|
+-- /artifacts/
|   +-- code/
|   +-- docs/
|   +-- tests/
|   +-- reports/
|   +-- builds/
|
+-- /logs/
    +-- agent_logs/
    +-- human_logs/
```

---

### Universal File Schema

Every step output file follows this top-level structure. No field may be omitted.

```json
{
  "schema_version": "1.0",
  "run_id": "",
  "step_id": "",
  "step_name": "",
  "status": "",
  "input_file": "",
  "input_checksum": "sha256:...",
  "created_at": "",
  "actor": {
    "mode": "agent|human|hybrid",
    "actor_id": "",
    "reviewer_id": ""
  },
  "objective": {
    "task_name": "",
    "task_type": "",
    "success_definition": ""
  },
  "payload": {},
  "artifacts": [],
  "metrics": {},
  "hil": {
    "required": false,
    "status": "not_required|pending|approved|rejected",
    "review_notes": "",
    "approved_by": "",
    "approved_at": "",
    "rejection_reason": ""
  },
  "decision": {
    "result": "pass|fail|needs_revision|needs_human|blocked",
    "reason": "",
    "next_step": ""
  },
  "trace": {
    "assumptions": [],
    "risks": [],
    "changes": [],
    "lineage": []
  }
}
```

---

### Step 00: REQUEST NORMALIZATION

**Input:** `request.md`
**Output:** `00_request.json`

Convert a human-written prompt into structured machine-readable input.

**Payload:**
```json
{
  "request_text": "",
  "goal": "",
  "constraints": [],
  "desired_outputs": [],
  "priority": "low|medium|high|critical",
  "deadline": "",
  "stakeholders": [],
  "task_class": "build|optimize|research|ops|content|other"
}
```

**Metrics:**
```json
{
  "ambiguity_count": 0,
  "missing_field_count": 0,
  "normalization_confidence": 0
}
```

**HIL rule:** If ambiguity is above threshold, `decision.result = "needs_human"`.

---

### Step 01: INTAKE (DEFINE)

**Input:** `00_request.json`
**Output:** `01_intake.json`

Define exactly what the task is. This is where the task is actually understood.

**Payload:**
```json
{
  "problem_statement": "",
  "scope_in": [],
  "scope_out": [],
  "success_metrics": [],
  "constraints": [],
  "stakeholders": [],
  "done_definition": [],
  "risk_level": "low|medium|high"
}
```

**Metrics:**
```json
{
  "scope_clarity_score": 0,
  "success_metric_count": 0,
  "constraint_count": 0,
  "risk_score": 0
}
```

**Pass condition:** Cannot pass unless problem statement exists, at least 1 success metric exists, and done definition exists.

---

### Step 01a: WORKFLOW RESOLUTION

**Input:** `01_intake.json`
**Output:** `01a_workflow_resolution.json`

Turn task characteristics into an execution path. This is the controller for the entire runtime.

**Payload:**
```json
{
  "task_class": "",
  "estimated_complexity": "low|medium|high",
  "risk_level": "low|medium|high",
  "reversibility": "easy|moderate|hard",
  "production_impact": false,
  "customer_impact": false,
  "compliance_impact": false,
  "hil_required": false,
  "execution_profile": "TRIVIAL|STANDARD|COMPLEX|HIGH_RISK",
  "materialized_steps": [],
  "collapsed_steps": {},
  "skipped_steps": [],
  "skip_reasons": {},
  "routing": {
    "next_step_file": ""
  }
}
```

**Validation rule:** This step is produced by Agent A and validated by Agent B. If they disagree, `decision.result = "needs_human"`.

**Escalation rule:** If at any later step the actual complexity exceeds what 01a predicted, that step may request re-routing by outputting `decision.result = "needs_revision"` with `decision.next_step = "01a_workflow_resolution.json"`. A new `01a_workflow_resolution_v2.json` is produced without scrapping completed work.

---

### Step 02: DECOMPOSE (BREAK DOWN)

**Input:** `01a_workflow_resolution.json`
**Output:** `02_decompose.json`

Break the task into measurable work units.

**Payload:**
```json
{
  "workstreams": [
    {
      "id": "WS-01",
      "name": "",
      "description": "",
      "dependencies": [],
      "acceptance_criteria": []
    }
  ],
  "critical_path": [],
  "required_roles": [],
  "estimated_complexity": "low|medium|high",
  "task_graph_summary": ""
}
```

**Metrics:**
```json
{
  "workstream_count": 0,
  "dependency_count": 0,
  "acceptance_criteria_count": 0,
  "decomposition_coverage_score": 0
}
```

**Pass condition:** Each workstream must have id, owner role, dependencies, and acceptance criteria.

---

### Step 03: SELECT SYSTEM (DMADV / DMAIC)

**Input:** `02_decompose.json`
**Output:** `03_system_select.json`

Select the operating framework for the job.

**Payload:**
```json
{
  "primary_system": "DMADV|DMAIC",
  "selection_reason": "",
  "secondary_loops": [],
  "system_mapping": {
    "define_phase": "",
    "measure_phase": "",
    "analyze_phase": "",
    "build_or_improve_phase": "",
    "verify_or_control_phase": ""
  }
}
```

**Metrics:**
```json
{
  "selection_confidence": 0,
  "fit_score": 0,
  "uncertainty_count": 0
}
```

**HIL rule:** If confidence is below threshold, require human approval.

**System guide:**
- **DMADV** (Define, Measure, Analyze, Design, Verify) — for building something new
- **DMAIC** (Define, Measure, Analyze, Improve, Control) — for improving something existing

---

### Step 04: DESIGN (PLAN SOLUTION)

**Input:** `03_system_select.json`
**Output:** `04_design.json`

Produce the execution blueprint.

**Payload:**
```json
{
  "solution_architecture": "",
  "interfaces": [],
  "editable_surfaces": [],
  "immutable_surfaces": [],
  "artifact_plan": [],
  "test_plan": [],
  "handoff_plan": [],
  "failure_modes": [],
  "hil_checkpoints": []
}
```

**Metrics:**
```json
{
  "design_completeness_score": 0,
  "interface_count": 0,
  "test_coverage_planned": 0,
  "unresolved_risk_count": 0
}
```

**Pass condition:** Cannot pass unless it defines what may be changed, what may not be changed, how success will be tested, and where HIL approval is required.

---

### Step 02_03_04: PLAN (Collapsed)

**Used in:** TRIVIAL profile only.

**Input:** `01a_workflow_resolution.json`
**Output:** `02_03_04_plan.json`

Combines decomposition, system selection, and design into a single manifest for low-complexity work.

**Payload:**
```json
{
  "decomposition": {
    "workstreams": [],
    "critical_path": []
  },
  "system_selection": {
    "primary_system": "",
    "selection_reason": ""
  },
  "design": {
    "solution_summary": "",
    "editable_surfaces": [],
    "immutable_surfaces": [],
    "test_plan": []
  }
}
```

---

### Step 05: EXECUTE (BUILD)

**Input:** `04_design.json` (or `02_03_04_plan.json`)
**Output:** `05_execute.json`

Carry out the work. This step may create many files, but the only official handoff is this manifest. It must reference every produced artifact.

**Payload:**
```json
{
  "execution_summary": "",
  "completed_workstreams": [],
  "partial_workstreams": [],
  "blocked_workstreams": [],
  "change_manifest": [],
  "artifact_manifest": [],
  "local_validation_results": [],
  "open_issues": []
}
```

**Metrics:**
```json
{
  "completion_rate": 0,
  "build_success_rate": 0,
  "local_test_pass_rate": 0,
  "defect_count_known": 0,
  "execution_time_sec": 0,
  "human_minutes": 0,
  "api_cost_usd": 0
}
```

---

### Step 06: REVIEW (CHECK)

**Input:** `05_execute.json`
**Output:** `06_review.json`

The main gatekeeping step. Runs evaluation and produces a single unambiguous result.

**Payload:**
```json
{
  "review_summary": "",
  "evaluation_results": [],
  "defects_found": [],
  "compliance_findings": [],
  "safety_findings": [],
  "performance_findings": [],
  "decision_basis": ""
}
```

**Metrics:**
```json
{
  "overall_pass_rate": 0,
  "defect_count": 0,
  "severity_score": 0,
  "benchmark_delta": 0,
  "compliance_score": 0,
  "review_cycle_time_sec": 0
}
```

**Allowed outcomes:**
```
pass           -> 07_integrate.json
needs_revision -> back to 04_design.json or 05_execute.json
needs_human    -> hold until human approval
fail           -> close or redesign
blocked        -> escalate
```

---

### Step 07: INTEGRATE (ASSEMBLE)

**Input:** `06_review.json`
**Output:** `07_integrate.json`

Merge accepted outputs into one coherent deliverable.

**Payload:**
```json
{
  "integration_summary": "",
  "accepted_components": [],
  "rejected_components": [],
  "resolved_dependencies": [],
  "release_candidate_manifest": [],
  "documentation_status": "",
  "integration_test_results": []
}
```

**Metrics:**
```json
{
  "integration_success_rate": 0,
  "conflict_count": 0,
  "artifact_completeness_score": 0,
  "release_readiness_score": 0
}
```

**Pass condition:** Cannot pass unless accepted components are listed, release candidate exists, and integration tests are recorded.

---

### Step 08: IMPROVE (LEARN)

**Input:** `07_integrate.json` (or `06_review.json` if 07 was skipped)
**Output:** `08_improve.json`

Capture process learning. This step updates the system, not just the current deliverable.

**Payload:**
```json
{
  "what_worked": [],
  "what_failed": [],
  "bottlenecks": [],
  "recommended_process_changes": [],
  "recommended_template_changes": [],
  "next_iteration_backlog": []
}
```

**Metrics:**
```json
{
  "bottleneck_count": 0,
  "estimated_waste_score": 0,
  "reuse_opportunity_count": 0,
  "improvement_priority_score": 0
}
```

---

### Step 09: PROMOTE (DEPLOY)

**Input:** `08_improve.json`
**Output:** `09_promote.json`

Promote to environment, verify, and close the run.

**Payload:**
```json
{
  "promotion_target": "experiment|candidate|staging|production",
  "deployment_summary": "",
  "deployment_artifacts": [],
  "smoke_test_results": [],
  "rollback_plan": "",
  "rollback_ready": true,
  "final_release_status": "deployed|rejected|rolled_back"
}
```

**Metrics:**
```json
{
  "deploy_duration_sec": 0,
  "smoke_test_pass_rate": 0,
  "incident_count": 0,
  "rollback_time_estimate_sec": 0,
  "promotion_confidence": 0
}
```

**Pass condition:** Cannot be marked complete unless smoke tests ran, rollback plan exists, and final release status is explicit.

---

## Part 9: Human-in-the-Loop Standard

HIL is not a separate side workflow. It is a required section in every file.

```json
"hil": {
  "required": true,
  "status": "pending",
  "review_notes": "Need signoff on selected framework.",
  "approved_by": "",
  "approved_at": "",
  "rejection_reason": ""
}
```

### HIL States

| State | Meaning |
|-------|---------|
| `not_required` | No human review needed for this step |
| `pending` | Waiting on human review |
| `approved` | Human approved, workflow may advance |
| `rejected` | Human rejected, see rejection_reason |

### Rule

If `hil.required = true` and `hil.status != approved`, the workflow **may not advance**.

---

## Part 10: Mid-Run Escalation

When the execution profile is wrong mid-run — TRIVIAL turns out to be COMPLEX — the system needs an escalation path.

Any step may output:
```json
"decision": {
  "result": "needs_revision",
  "reason": "Scope expanded — discovered 3 additional dependent systems",
  "next_step": "01a_workflow_resolution.json"
}
```

This triggers a new `01a_workflow_resolution_v2.json` that re-routes without scrapping completed work. All prior step outputs remain valid. Only the forward path changes.

---

## Part 11: Transparency Standard

Every output file must be sufficient for an auditor to answer:

1. What came in?
2. Who processed it?
3. What changed?
4. What was produced?
5. What was measured?
6. Was a human involved?
7. What is the decision?
8. What happens next?

If a file cannot answer all 8, it fails schema compliance.

---

## Part 12: State Machine Summary

### Full canonical workflow
```
00 -> 01 -> 01a -> 02 -> 03 -> 04 -> 05 -> 06 -> 07 -> 08 -> 09
```

### Runtime profiles
```
TRIVIAL:    00 -> 01 -> 01a -> 02_03_04 -> 05 -> 06 -> 08
STANDARD:   00 -> 01 -> 01a -> 02 -> 03 -> 04 -> 05 -> 06 -> 07 -> 08
COMPLEX:    00 -> 01 -> 01a -> 02 -> 03 -> 04 -> 05 -> 06 -> 07 -> 08 -> 09
HIGH_RISK:  00 -> 01 -> 01a -> 02 -> 03 -> 04 -> 05 -> 06 -> 07 -> 08 -> 09
            (with mandatory HIL at 03, 04, 06, 09)
```

### Review routing
```
06_review.json
  -> pass           -> 07_integrate.json
  -> needs_revision -> 04_design.json or 05_execute.json
  -> needs_human    -> hold until approval
  -> fail           -> close or redesign
  -> blocked        -> escalate
```

---

## Part 13: The Simplest Version

```
Each numbered step outputs one JSON manifest.

That manifest contains:
1. Identity      - what step, what run
2. Lineage       - where it came from
3. Payload       - the actual work content
4. Artifacts     - references to produced files
5. Metrics       - measurable outcomes
6. HIL status    - human involvement record
7. Decision      - pass/fail/revise/blocked
8. Next step     - where the workflow goes

Two agents gate the entry.
Four profiles scale the ceremony.
One review step gates the exit.

Same framework for a typo fix and a product launch.
```

---

## Part 14: Mandatory File Trail Rules

These rules are non-negotiable. They exist because the first real run skipped mandatory files and broke auditability.

### Rule M1: Every run must produce ALL never-skip files

The following files must exist in every run directory, regardless of profile:

| File | Why it cannot be skipped |
|------|-------------------------|
| `00_request.json` | What was asked — without this, no audit trail exists |
| `01_intake.json` | What it means — without this, scope and success criteria are undefined |
| `01a_workflow_resolution.json` | Why this path was chosen — without this, step skipping is undocumented |
| `05_execute.json` | What was done — without this, no record of changes exists |
| `06_review.json` | Whether it passed — without this, quality is unmeasured |

If any of these 5 files is missing, the run is **invalid** and cannot be scored.

### Rule M2: No step may execute without its input file existing

A worker agent must not produce step N's output unless step N-1's output file exists in the run directory. The runner enforces this — it refuses to advance if the prior step file is missing.

Exception: `00_request.json` reads from `request.md` (not a JSON manifest).

### Rule M3: Every skipped step must be documented in 01a

If a step is not materialized, `01a_workflow_resolution.json` must list it in `skipped_steps` with a reason in `skip_reasons`. Silent skipping is a schema violation.

### Rule M4: The runner must validate before advancing

Before any step N+1 begins, the runner must validate step N's output against JSON Schema. If validation fails, the workflow halts. No manual bypass.

### Rule M5: 06_review.json must contain the quality section

Step 06 is not just a pass/fail decision. It must include the `quality.spec_compliance` and `quality.regression` sections. A review file without quality measurement fails schema validation.

### Rule M6: One input, one output, no exceptions

Each step reads exactly one file and writes exactly one file. Artifacts (code, docs, tests) are referenced inside the manifest but are not the step output. If an agent produces code but not the manifest, the step is incomplete.

### Rule M7: Run directory must be created before any work begins

`runner.py new` must be called first. It creates the directory, subdirectories, and `request.md`. No agent may write step files into an ad-hoc location.

### Rule M8: Completed runs must be scored

After the final step, `evaluate.py <run_dir> --log` must run. An unscored run cannot contribute to learning metrics. The supervisor loop enforces this automatically.

### Enforcement

The runner (`runner.py`) enforces M1-M4 at runtime. The evaluator (`evaluate.py`) enforces M5 and M8 at scoring time. M6 and M7 are structural — violated only by bypassing the tooling entirely.

If an agent bypasses the runner and writes files directly, the `audit_complete` gate will fail when the run is scored, and the run will be marked invalid.

---

## Part 15: Grading Framework

The supervisor grades every run across 4 gates, 1 primary metric, 3 learning metrics, and 4 operational trackers.

### Gates (Binary — must all pass or run is invalid)

| Gate | What it checks | How |
|------|---------------|-----|
| **Schema compliance** | Every step file validates against JSON Schema | All mandatory fields present, correct types, valid enums |
| **Spec adherence** | All `done_definition` items from step 01 are met | Compare 01_intake.done_definition against 06_review.quality.spec_compliance |
| **No regressions** | Build passes, tests pass, nothing outside scope changed | 06_review.quality.regression.build_passes AND tests_pass AND files_outside_scope_changed is empty |
| **Audit completeness** | All materialized steps from 01a actually exist as files | Every entry in 01a.materialized_steps has a corresponding file in the run directory |

If any gate fails, the run is invalid regardless of other scores.

### Primary Metric: `run_quality`

```
run_quality = (spec_compliance_score * 0.4) + (regression_score * 0.3) + (acceptance_score * 0.3)
```

| Component | Source | Definition |
|-----------|--------|-----------|
| `spec_compliance_score` | 06_review.quality.spec_compliance.score | `items_met / items_defined` — binary per item, no partial credit |
| `regression_score` | 06_review.quality.regression.score | `1.0` if build passes + tests pass + no scope violations. `0.0` if any fail |
| `acceptance_score` | Human follow-up (7 days post-delivery) | `1.0` = accepted, no changes. `0.7` = minor tweaks. `0.3` = substantially reworked. `0.0` = rejected |

### Quality Measurement in Step 06

Step 06 (review) must include a `quality` section in its payload:

```json
"quality": {
  "spec_compliance": {
    "items_defined": 4,
    "items_met": 4,
    "score": 1.0,
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

Layer 3 (acceptance) is captured retroactively after the run closes:

```json
"quality_followup": {
  "measured_at": "2026-03-31T00:00:00Z",
  "human_revisions": 0,
  "human_rework": false,
  "survived_days": 7,
  "acceptance_score": 1.0
}
```

### Learning Metrics (tracked over time, not per-run)

| Metric | Formula | What it tells you |
|--------|---------|-------------------|
| `profile_accuracy` | `runs_with_no_escalation / total_runs` | Is routing getting better? |
| `net_improvement_rate` | `(avg_quality_recent - avg_quality_early) / iterations` | Is the system actually improving? |
| `reliability` | `1 - stddev(run_quality across similar tasks)` | Is quality consistent or erratic? |

If `profile_accuracy` flatlines: routing expertise isn't learning.
If `net_improvement_rate` is zero: the learning loop is generating motion, not progress.
If `reliability` is low: the system is inconsistent — investigate worker variance.

### Operational Trackers (tracked but not optimized yet)

| Tracker | Source |
|---------|--------|
| `cycle_time_sec` | 05_execute.metrics.execution_time_sec |
| `cost_usd` | 05_execute.metrics.api_cost_usd |
| `tokens_total` | Summed from agent logs |
| `human_override_count` | Count of HIL rejections + Agent B disagreements across the run |

These are informational. Do not optimize them until run_quality is consistently above 0.8.

### Post-Run Categorization

After every run, the supervisor compares predictions vs reality:

```json
{
  "run_id": "RUN-2026-03-24-001",
  "predicted": {
    "task_class": "bug_fix",
    "complexity": "low",
    "risk": "low",
    "profile": "TRIVIAL"
  },
  "actual": {
    "task_class": "bug_fix",
    "complexity": "medium",
    "risk": "low",
    "profile_needed": "STANDARD",
    "escalated": true,
    "escalation_reason": "discovered 3 dependent files",
    "revision_loops": 0,
    "human_overrides": 0
  },
  "lesson": "bug fixes touching auth always cascade — route to STANDARD minimum",
  "applies_to": "task_class=bug_fix AND scope_touches=auth"
}
```

Mismatches become routing rules in the expertise file. Rules are applied on future runs and tracked for effectiveness. Rules with success rate below 30% after 5+ applications are retired.

---

## Part 15: Supervisor Infrastructure

The O&P system is implemented as an autoresearch agent at `autoresearch/oandp/`.

### Directory Structure

```
autoresearch/oandp/
  schemas/              # JSON Schema for every step (13 files)
  expertise/            # Learned routing rules (routing_rules.jsonl)
  runs/                 # Completed run directories
  reports/              # Final reports
  pending_requests/     # Drop request.md files here to trigger runs
  logs/                 # Agent and human logs
  profiles.json         # 4 execution profiles + routing logic
  evaluate.py           # Scoring: 4 gates + primary metric + learning metrics
  runner.py             # State machine: validates, enforces order, blocks HIL
  program.md            # Supervisor agent instructions
  run.sh                # Outer restart loop
  loop.log              # Human-readable log
  runs.jsonl            # Machine-readable per-run log
  metrics.jsonl         # Scored run results
  run_categorizations.jsonl  # Prediction vs reality comparisons
```

### Commands

```bash
# Create a new run
python3 autoresearch/oandp/runner.py new "Add dark mode to settings"

# Check run status
python3 autoresearch/oandp/runner.py status autoresearch/oandp/runs/<run_id>

# Validate a step file
python3 autoresearch/oandp/runner.py validate <run_dir> 05_execute.json

# Score a completed run
python3 autoresearch/oandp/evaluate.py <run_dir> --log

# View learning metrics across all runs
python3 autoresearch/oandp/evaluate.py --aggregate autoresearch/oandp/metrics.jsonl

# Start the supervisor loop
bash autoresearch/oandp/run.sh

# Stop the supervisor
touch /tmp/gw-oandp.stop

# Monitor
tail -f autoresearch/oandp/loop.log
```
