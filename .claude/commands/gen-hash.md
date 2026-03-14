---
description: Generate a cryptographically secure key for webhook auth, API secrets, or admin tokens
user-invokable: true
args:
  - name: label
    description: Name for the key (e.g. REVENUECAT_WEBHOOK_AUTH_KEY, ADMIN_TOKEN)
    required: false
---

Generate a secure random key and display it with copy instructions.

Run the gen-hash tool:

```bash
python3 $CLAUDE_PROJECT_DIR/.claude/tools/gen-hash {{ label | default: "WEBHOOK_AUTH_KEY" }}
```

Show the output to the user. If they ask to generate multiple keys, run the tool once per key with different labels.
