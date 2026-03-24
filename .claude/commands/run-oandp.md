---
description: "Start/stop/status the O&P workflow supervisor. Manage runs, score results, view learning metrics."
allowed-tools: ["Bash", "Read"]
---

# Run O&P Supervisor

## Check Status

```bash
PID_FILE="/tmp/gw-oandp.pid"
if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
    echo "RUNNING (PID $(cat $PID_FILE))"
    echo ""
    echo "Recent activity:"
    tail -20 autoresearch/oandp/loop.log
else
    echo "NOT RUNNING"
    echo ""
    echo "Recent runs:"
    python3 autoresearch/oandp/runner.py list 2>/dev/null || echo "No runs yet"
fi
```

## Commands

Based on user request, execute the appropriate command:

### Start the supervisor
```bash
bash autoresearch/oandp/run.sh
```

### Stop the supervisor
```bash
touch /tmp/gw-oandp.stop
echo "Stop signal sent. Supervisor will generate final report and exit."
```

### Submit a new task
```bash
# Create a pending request file
echo "# Request\n\n<user's request text>" > autoresearch/oandp/pending_requests/request_$(date +%Y%m%d_%H%M%S).md
echo "Request submitted. Start supervisor to process it."
```

### Manual run (without supervisor)
```bash
python3 autoresearch/oandp/runner.py new "<request text>"
```

### Check run status
```bash
python3 autoresearch/oandp/runner.py status <run_dir>
```

### Score a completed run
```bash
python3 autoresearch/oandp/evaluate.py <run_dir> --log
```

### View learning metrics
```bash
python3 autoresearch/oandp/evaluate.py --aggregate autoresearch/oandp/metrics.jsonl
```

### List all runs
```bash
python3 autoresearch/oandp/runner.py list
```

### View final report
```bash
cat autoresearch/oandp/reports/FINAL-REPORT.md
```

### Monitor live
```bash
tail -f autoresearch/oandp/loop.log
```
