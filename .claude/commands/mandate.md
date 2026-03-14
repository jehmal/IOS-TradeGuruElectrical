---
description: "Add a mandatory rule to CLAUDE.md, AGENTS.md, or other AI instruction files. Usage: /mandate <rule to add>"
allowed-tools: ["Read", "Glob", "Edit", "Write"]
---

# Mandate: $ARGUMENTS

Add this rule to the Mandatory section of the project's AI instruction file.

## Step 1: Find the target file

Search for these files in order of priority. Use the FIRST one found:
1. `CLAUDE.md` (project root)
2. `AGENTS.md` (project root)
3. `gemini.md` (project root)
4. `.claude/CLAUDE.md`
5. `.cursor/rules.md`

If none exist, create `CLAUDE.md` at project root.

## Step 2: Check for existing Mandatory section

Read the file. Look for a section headed `## Mandatory` (any heading level: #, ##, ###).

**If it exists:** Append the new rule as the next numbered item at the end of the list.

**If it does NOT exist:** Add this section at the TOP of the file, immediately after any frontmatter or title:

```markdown
## Mandatory

1. $ARGUMENTS
```

## Step 3: Format the rule

- If the user's text is a single sentence, add it as-is with a number prefix
- If it contains multiple sentences, keep them together as one numbered item
- Do not rephrase or soften the rule — add it verbatim
- Use the next sequential number after the last existing mandatory item

## Step 4: Confirm

Tell the user: which file was edited, the rule number assigned, and quote the rule back.
