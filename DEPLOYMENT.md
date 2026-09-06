# RentFlow AI — Production Deployment Guide

## 1. Prerequisites
- **Operating System**: Linux (Ubuntu 22.04 LTS recommended) or container orchestrator (Kubernetes / AWS ECS / Google Cloud Run).
- **Docker Engine**: Version 24.0+ and Docker Compose v2+.
- **Java Runtime** (for non-containerized runs): OpenJDK 17 or Eclipse Temurin 17.
- **Node.js** (for frontend build): Node.js v18+ or v20 LTS.
- **Database**: PostgreSQL 14+ (managed RDS/Cloud SQL recommended).

---

## 2. Environment Variables Specification

All production deployments require the following environment variables. The application uses fail-fast validation and will refuse to boot if required secrets or production safety constraints are missing.

| Variable Name | Required | Default / Example | Purpose |
| :--- | :---: | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | **YES** | `prod` | Activates production configuration profile |
| `DB_HOST` | **YES** | `postgres.prod.internal` | PostgreSQL database host |
| `DB_PORT` | NO | `5432` | PostgreSQL database port |
| `DB_NAME` | **YES** | `rentflow_prod` | PostgreSQL database name |
| `DB_USER` | **YES** | `rentflow_app` | Database username with least privilege |
| `DB_PASSWORD` | **YES** | `(secret)` | Database user password |
| `JWT_SECRET` | **YES** | `(min 32-char high-entropy secret)` | HMAC SHA-256 signing secret for authentication tokens |
| `JWT_EXPIRATION` | NO | `86400000` (24 hours) | Token time-to-live in milliseconds |
| `CORS_ALLOWED_ORIGINS` | **YES** | `https://app.rentflow.ai,https://portal.rentflow.ai` | Comma-separated list of allowed web origins |
| `GEMINI_API_KEY` | NO | `AIzaSy...` | Google Gemini API key for live AI sales & copilot features |
| `TELEPHONY_PROVIDER` | NO | `mock` or `twilio` | Active telephony driver for Phone AI |
| `TELEPHONY_WEBHOOK_SECRET`| NO | `(secret)` | Signature secret for verifying provider inbound webhooks |

---

## 3. Docker Compose Deployment (Recommended Quick Start)

Clone the repository and launch the containerized stack:

```bash
# 1. Create your production environment file
cp .env.example .env

# Edit .env and supply your real database password, JWT secret, and CORS origins
nano .env

# 2. Build and launch services in detached mode
docker compose up -d --build

# 3. Verify services are running and healthy
docker compose ps

# 4. Tail production logs
docker compose logs -f backend
```

---

## 4. Kubernetes Deployment Architecture

In Kubernetes, RentFlow AI is deployed via standard manifests:
- **`Deployment: rentflow-backend`**: 3 replicas, resource requests (`cpu: 500m, memory: 1Gi`), resource limits (`cpu: 2, memory: 2Gi`).
- **`Liveness Probe`**: HTTP GET `/api/health` on port 8080.
- **`Readiness Probe`**: HTTP GET `/api/health/ready` on port 8080 (verifies PostgreSQL connectivity).
- **`Secret: rentflow-secrets`**: Stores `DB_PASSWORD`, `JWT_SECRET`, and `GEMINI_API_KEY`.
- **`Ingress`**: TLS termination via cert-manager, HTTP to HTTPS redirect, rate limiting annotation.

---

## 5. Reverse Proxy Configuration (Nginx)

When deploying behind an external Nginx proxy, ensure the following headers are passed:

```nginx
server {
    listen 443 ssl http2;
    server_name app.rentflow.ai;

    ssl_certificate /etc/letsencrypt/live/app.rentflow.ai/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/app.rentflow.ai/privkey.pem;

    # Static Angular SPA frontend
    location / {
        root /var/www/rentflow/frontend;
        try_files $uri $uri/ /index.html;
    }

    # Reverse proxy to Spring Boot backend
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_read_timeout 90s;
    }
}
```

---

## 6. Zero-Downtime Rollback Procedure
1. If health check probes fail after a new deployment, traffic is immediately diverted to the previous replica set.
2. In Docker Compose:
   ```bash
   docker compose rollback backend
   # Or switch back to previous tagged image:
   docker tag rentflow-backend:v0.1.0-previous rentflow-backend:latest
   docker compose up -d backend
   ```
3. Database migrations (Flyway/Liquibase) are backwards-compatible (expand-and-contract pattern) to prevent breaking running instances.
