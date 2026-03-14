---
description: "Factcheck OpenAI API usage against official docs using Codex CLI. Usage: /get-openai-factcheck <what to verify>"
allowed-tools: ["Read", "Glob", "Grep", "Write", "Bash"]
---

# OpenAI Factcheck: $ARGUMENTS

You are a factcheck dispatcher. The user wants to verify something against OpenAI's official API documentation. You use the Codex CLI programmatically to research and produce a factcheck report.

## Step 1: Understand what needs checking

The user wants to verify:
> $ARGUMENTS

Determine:
- **Files to check** — glob for relevant files in the project (inventory docs, Swift files, config files, service files)
- **Specific claims** — endpoints, parameter names, request/response shapes, model names, feature availability
- **Scope** — is this about one endpoint or a full inventory?

## Step 2: Build the Codex prompt

Construct a detailed factcheck prompt that tells Codex exactly what to verify. The prompt must:
- Reference specific files by path for Codex to read
- List every claim to verify
- Ask Codex to use web search against official OpenAI documentation
- Request a structured verdict for each claim

## Step 3: Run Codex

Determine the output file number:
- Glob `docs/openai-factcheck*.md` to find existing reports
- Number the new one sequentially

Execute Codex programmatically:

```bash
cat <<'PROMPT' | codex -a never exec --skip-git-repo-check --sandbox workspace-write --output-last-message docs/openai-factcheck-<N>.md -C /mnt/c/users/jehma/desktop/Tradeguru-swft -
<your constructed prompt here>
PROMPT
```

Run this command in the background using `run_in_background: true` so the user isn't blocked.

## Step 4: Report to user

Tell the user:
- Codex is running in the background
- Where the report will be saved: `docs/openai-factcheck-<N>.md`
- What it's checking
- You'll notify them when it's done

## Codex prompt template

Use this structure for the prompt you send to Codex:

```
You are a fact-checker. Verify the following against official OpenAI API documentation.

1. Read these files:
<list of files to read>

2. For each claim, research the official OpenAI docs and verify:
- Is the endpoint URL correct?
- Is the HTTP method correct?
- Are parameter names and types accurate?
- Are response shapes accurate?
- Is anything deprecated or superseded?
- Are there missing required parameters?

3. Output a factcheck report as markdown:

# OpenAI Factcheck Report #<N>

**Date:** <date>
**Scope:** <what was checked>
**Source files:** <files read>

## Verdicts

### Claim N: <description>
- **Status:** CORRECT / INCORRECT / PARTIALLY CORRECT
- **Details:** <specifics>
- **Fix:** <what to change, if anything>

## Summary
- Claims checked: N
- Correct: N
- Issues: N
- Critical (runtime failures): N
- Warnings (suboptimal): N

Use web search to access the latest OpenAI API documentation. Be thorough and precise.
```

## Rules

- Always use `--skip-git-repo-check` (project may not be a git repo)
- Always use `--sandbox workspace-write` for safety
- Always use `--output-last-message` to save the report
- Always run in background so the user isn't blocked
- The Codex prompt must be specific — don't send vague "check everything" requests
- Include file paths in the prompt so Codex reads the actual code/docs
- Number reports sequentially in `docs/`
