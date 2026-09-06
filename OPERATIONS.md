# RentFlow AI — Production Operations & Runbook

## 1. Daily Health Checks & Monitoring
Operate the platform with standard observability probes:

- **Liveness Probe**: `GET /api/health`
  - Expected Response: `{"application":"RentFlow AI","status":"UP","version":"0.1.0","timestamp":"..."}` with HTTP 200.
  - Failure Action: Restart container pod if unresponsive.
- **Readiness Probe**: `GET /api/health/ready`
  - Expected Response: `{"timestamp":"...","status":"READY","database":"CONNECTED"}` with HTTP 200.
  - Failure Action: Alert on-call SRE; verify PostgreSQL connectivity, connection pool exhaustion, or network partitioning.

---

## 2. Backup & Restore Runbook

### Automated PostgreSQL Nightly Backups
```bash
# Create timestamped database backup
pg_dump -h $DB_HOST -U $DB_USER -d rentflow_prod -F c -b -v -f /backups/rentflow_$(date +%Y%m%d_%H%M%S).dump

# Encrypt backup using GPG
gpg --symmetric --cipher-algo AES256 /backups/rentflow_*.dump

# Transfer to encrypted cold cloud storage bucket (AWS S3 / GCP Cloud Storage)
aws s3 cp /backups/rentflow_*.dump.gpg s3://rentflow-prod-backups/daily/
```

### Database Restoration Procedure
```bash
# 1. Stop backend application to prevent write conflicts
docker compose stop backend

# 2. Decrypt backup file
gpg --decrypt /backups/rentflow_20260906.dump.gpg > /backups/rentflow_restore.dump

# 3. Restore database schema and data
pg_restore -h $DB_HOST -U $DB_USER -d rentflow_prod --clean --if-exists -v /backups/rentflow_restore.dump

# 4. Restart backend application and verify readiness
docker compose start backend
curl -i http://localhost:8080/api/health/ready
```

---

## 3. Incident Management & Disaster Recovery Checklists

### Incident 1: Database Connection Pool Starvation
- **Symptoms**: Readiness probe returns HTTP 503 `{"status":"NOT_READY","database":"DISCONNECTED"}`.
- **Root Causes**: Long-running reporting queries, uncommitted transaction leaks, network latency.
- **Action Steps**:
  1. Inspect active connections: `SELECT pid, now() - pg_stat_activity.query_start AS duration, query, state FROM pg_stat_activity WHERE state != 'idle';`
  2. Terminate stuck locking queries: `SELECT pg_terminate_backend(pid);`
  3. Increase HikariCP maximum pool size if legitimate load has grown: `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=30`.

### Incident 2: Telephony Provider Webhook Failures
- **Symptoms**: Inbound phone calls not appearing in the Phone AI dashboard or calls failing to connect.
- **Action Steps**:
  1. Check telephony webhook logs: `docker compose logs --tail=100 backend | grep "webhook"`
  2. Verify telephony secret configuration: `TELEPHONY_WEBHOOK_SECRET` matches provider webhook settings.
  3. Check signature verification headers: `X-Twilio-Signature` or provider equivalent.

### Incident 3: AI Rate Limit or Quota Exhaustion
- **Symptoms**: AI Sales Agent or Copilot logs show 429 quota errors from Gemini API.
- **Action Steps**:
  1. System automatically falls back to deterministic rule-based replies and cached catalog data.
  2. Sales and booking workflows remain 100% operational; AI is never the blocker for quotes or checkout.
  3. Rotate API key via `GEMINI_API_KEY` or increase API project quota in Google Cloud Console.
