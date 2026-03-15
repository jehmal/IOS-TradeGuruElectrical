# Endpoint Test: workos-auth-test-8-device-auth-still-works

**Tested:** 2026-03-15T00:00:00Z
**URL:** POST https://tradeguru.com.au/api/v1/chat
**Auth:** device
**Status:** PASS

## Request
```bash
curl -s -w "\n%{http_code}\n%{content_type}" -N --max-time 30 -X POST https://tradeguru.com.au/api/v1/chat \
  -H "Content-Type: application/json" \
  -H "X-Device-ID: ec5da159-51ff-4f38-8e96-25e683b253c0" \
  -d '{"messages":[{"role":"user","content":"test"}],"mode":"fault_finder","platform":"ios"}'
```

## Response
**HTTP Status:** 200
**Content-Type:** text/event-stream
```
event: status
data: {"stage":"searching"}

event: status
data: {"stage":"synthesizing"}

event: status
data: {"stage":"streaming"}

event: block
data: {"type":"text","content":"To perform a continuity test on submains, follow these steps to verify proper electrical connections prior to power connection."}

event: block
data: {"type":"step_list","steps":[{"step_number":1,"title":"Set Multimeter","description":"Switch your multimeter to continuity test mode.","safety_note":"Ensure supply is disconnected."},{"step_number":2,"title":"Test Active Conductor","description":"Check the active conductor from the meter to the main switch with the switch in the OFF position, then in the ON position."},{"step_number":3,"title":"Check Load Side","description":"Test from the load side of the main switch to verify continuity of the active conductor to the submains' end."},{"step_number":4,"title":"Verify Switching","description":"Toggle the main switch ON and OFF to confirm circuit integrity."}...
```

## Expectations
| Check | Expected | Actual | Result |
|-------|----------|--------|--------|
| Status | 200 | 200 | PASS |
| Content-Type | text/event-stream | text/event-stream | PASS |
| SSE events | status, block | status, block | PASS |

## Notes
Device auth still works correctly for regular chat endpoints after WorkOS auth endpoint tests.
