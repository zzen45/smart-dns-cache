# Reactive In-House DNS Cache API
[![Backend CI](https://github.com/zzen45/smart-dns-cache/actions/workflows/backend.yml/badge.svg)](https://github.com/zzen45/smart-dns-cache/actions)
## Full-Stack Demo
---

Spin up a reactive Spring Boot + Redis DNS‑cache microservice **and** a lightweight Angular dashboard with **one command** – no local Java/Node tooling required.

> 🧩 The frontend is an optional add-on, mainly for demo and fun UI purposes.  
> ✅ We recommend using Postman or `curl` to mimic real user/API requests.

---

## Quick Start

```bash
git clone https://github.com/zzen45/smart-dns-cache.git
cd smart-dns-cache
docker compose up --build
```

| Component          | Exposed URL                  |
|-------------------|------------------------------|
| Frontend (Angular + Nginx) | http://localhost/            |
| REST API (Spring Boot)     | http://localhost:8080/api/dns |
| Redis (Transparent)        | localhost:6379               |

> Ctrl‑C to stop, or `docker compose down -v` to stop and remove volumes.

---

## Dashboard (Optional UI)

Visit `http://localhost/` to:

- View live cache stats
- Add / delete records
- Flush the cache

---

## APIs Provided

```
GET    /api/dns/cache              # list all cached records
POST   /api/dns/cache              # add a manual record
DELETE /api/dns/cache/{domain}    # delete one record
DELETE /api/dns/cache             # clear everything
GET    /actuator/health           # system health
```

### Example (Postman or curl):
```bash
curl http://localhost:8080/api/dns/cache | jq

curl -X POST http://localhost:8080/api/dns/cache \
     -H "Content-Type: application/json" \
     -d '{"domain":"example.com","ip":"93.184.216.34","ttl":300}'
```

---

## Configuration

```
smart-dns-cache/
├─ docker-compose.yml
├─ dns-cache-ui/     # Angular dashboard (optional)
└─ dnscache/         # Spring Boot API (Java 17) + Redis client

┌────────┐  HTTP (Docker net)  ┌─────────────┐  TCP  ┌────────┐
│ Angular│ ───────────────────►│ Spring Boot │──────►│ Redis  │
│  UI    │  localhost (80)     │  API :8080  │ :6379 │ Cache  │
└────────┘                     └─────────────┘       └────────┘
```

- Change ports → edit `docker-compose.yml`
- Skip the UI → comment out `frontend:` service
- Change TTL/fallback servers → edit `dnscache/src/main/resources/application.yml`

---

## Development Setup (Without Docker)

Backend (Spring Boot):
```bash
cd dnscache
./mvnw spring-boot:run
```

Frontend (Angular):
```bash
cd dns-cache-ui
npm install
ng serve
```

- Angular UI → http://localhost:4200
- Spring API → http://localhost:8080/api/dns

---

## Git & CI Setup

- Monorepo tracks both backend and frontend
- `.gitignore` excludes build artifacts, IDE junk, and `node_modules`
- GitHub Actions auto-builds the backend on every push to `main`
- Frontend and Docker workflows can be added easily

---

## 🧹 Cleanup

```bash
docker compose down -v
docker image prune -f   # optional
```
