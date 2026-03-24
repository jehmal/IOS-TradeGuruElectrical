#!/bin/bash
# O&P Workflow Supervisor — outer restart loop
# Supervisor processes up to 3 runs then exits. This script restarts with fresh context.
# Supervisor reads loop.log on startup to see what was already done.
#
# Usage: bash autoresearch/oandp/run.sh
# Stop:  touch /tmp/gw-oandp.stop

set -e
AGENT_DIR="autoresearch/oandp"
PID_FILE="/tmp/gw-oandp.pid"
STOP_FILE="/tmp/gw-oandp.stop"
CHILD_PID=""
cd "$(git rev-parse --show-toplevel 2>/dev/null || pwd)"

if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
    echo "Already running (PID $(cat $PID_FILE))"; exit 1
fi

# MANDATORY: kill child Claude process when parent dies
cleanup() {
    if [ -n "$CHILD_PID" ] && kill -0 "$CHILD_PID" 2>/dev/null; then
        echo "[$(date '+%Y-%m-%d %H:%M')] Killing child Claude session (PID $CHILD_PID)" >> "$AGENT_DIR/loop.log"
        kill -- -"$CHILD_PID" 2>/dev/null || kill "$CHILD_PID" 2>/dev/null
        wait "$CHILD_PID" 2>/dev/null
    fi
    rm -f "$PID_FILE" "$STOP_FILE"
}
trap cleanup EXIT SIGTERM SIGINT SIGHUP

rm -f "$STOP_FILE"
echo $$ > "$PID_FILE"
echo "O&P Workflow Supervisor started (PID $$)"
echo "Monitor: tail -f $AGENT_DIR/loop.log"
echo "Stop: touch $STOP_FILE"

SESSION=1

generate_final_report() {
    echo "[$(date '+%Y-%m-%d %H:%M')] Generating final report..." >> "$AGENT_DIR/loop.log"
    claude --dangerously-skip-permissions \
        -p "You are the O&P supervisor shutting down. Generate a FINAL-REPORT.md in $AGENT_DIR/reports/. Read ALL of: $AGENT_DIR/loop.log, $AGENT_DIR/metrics.jsonl, $AGENT_DIR/run_categorizations.jsonl, $AGENT_DIR/expertise/routing_rules.jsonl. Write comprehensive final report covering: (1) Total runs processed, gate pass rates, avg run quality. (2) Routing accuracy trend. (3) Learned routing rules and their effectiveness. (4) Recommendations for process improvement." \
        >> "$AGENT_DIR/claude_output.log" 2>&1
    echo "[$(date '+%Y-%m-%d %H:%M')] Final report: $AGENT_DIR/reports/FINAL-REPORT.md" >> "$AGENT_DIR/loop.log"
}

while true; do
    if [ -f "$STOP_FILE" ]; then
        echo "=== STOPPING — generating final report ===" >> "$AGENT_DIR/loop.log"
        generate_final_report
        echo "=== STOPPED at session $SESSION ===" >> "$AGENT_DIR/loop.log"
        rm -f "$PID_FILE" "$STOP_FILE"; exit 0
    fi

    echo "" >> "$AGENT_DIR/loop.log"
    echo "=== SESSION $SESSION STARTED $(date '+%Y-%m-%d %H:%M') ===" >> "$AGENT_DIR/loop.log"

    claude --dangerously-skip-permissions \
        -p "Read $AGENT_DIR/program.md and execute the O&P supervisor loop. You are session $SESSION. Read $AGENT_DIR/loop.log FIRST. Check $AGENT_DIR/pending_requests/ for new requests. Process up to 3 runs. After EVERY run, write to BOTH $AGENT_DIR/loop.log AND $AGENT_DIR/runs.jsonl. After 3 runs or no pending requests, write session summary and exit." \
        >> "$AGENT_DIR/claude_output.log" 2>&1 &
    CHILD_PID=$!
    wait "$CHILD_PID" 2>/dev/null
    CHILD_PID=""

    if [ -f "$STOP_FILE" ]; then
        echo "=== STOPPING — generating final report ===" >> "$AGENT_DIR/loop.log"
        generate_final_report
        echo "=== STOPPED at session $SESSION ===" >> "$AGENT_DIR/loop.log"
        exit 0
    fi

    SESSION=$((SESSION + 1))
    sleep 10
done
