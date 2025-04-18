# 🧠⚡ Smart DNS Cache (full‑stack demo)

Spin up a reactive Spring Boot + Redis DNS‑cache API **and** a lightweight Angular
dashboard with **one command** – no local Java/Node tooling required.

## 🚀 Quick start

```bash
# 1 — clone the repo
git clone https://github.com/<you>/smart-dns-cache.git
cd smart-dns-cache

# 2 — build & run everything
docker compose up --build

Component	Exposed URL (after start‑up)
Front‑end (Angular + Nginx)	http://localhost/
REST API (Spring Boot)	http://localhost:8080/api/dns
Redis (transparent)	localhost:6379
Hit Ctrl‑C to stop, or docker compose down -v to stop and remove volumes.

🖥️ Dashboard (optional)

Open http://localhost/ to:

View live cache statistics
Add a manual DNS record
Delete individual entries
Flush the entire cache
Everything you do in the UI immediately calls the same API shown below.

🔍 API quick reference

GET  /api/dns/cache              # list all cached records
POST /api/dns/cache              # add manual record
DELETE /api/dns/cache/{domain}   # delete one record
DELETE /api/dns/cache            # clear all
GET  /actuator/health            # system health
Example (cURL / Postman)
# list cache
curl http://localhost:8080/api/dns/cache | jq

# add a manual entry
curl -X POST http://localhost:8080/api/dns/cache \
     -H "Content-Type: application/json" \
     -d '{"domain":"example.com","ip":"93.184.216.34","ttl":300}'
⚙️ Project structure

smart-dns-cache/
├─ docker-compose.yml   # Build file
├─ dns-cache-ui/        # Angular dashboard  (built → Nginx)
└─ dnscache/            # Spring Boot API + Redis client
How the pieces talk
┌────────┐  HTTP (Docker network)  ┌─────────────┐  TCP  ┌────────┐
│ Angular│ ───────────────────────►│ Spring Boot │──────►│ Redis  │
│  UI    │  localhost (80)         │  API :8080  │ :6379 │ Cache  │
└────────┘                         └─────────────┘       └────────┘
🛠️ Customisation

Change ports – edit the ports: lines inside docker-compose.yml.
Example: put the UI on 8081 instead of 80:
frontend:
  ports:
    - "8081:80"
Skip the UI – comment‑out or delete the frontend: service.
Change default TTL / fallback DNS servers – see dnscache/src/main/resources/application.yml.
🧹 Cleanup

docker compose down -v   # stop + remove volumes
docker image prune -f    # remove dangling images (optional)
✨ What you get

Immediate demo of a micro‑service talking to Redis and a separate UI – all containerised
No local Java, Node, or Redis installation headaches
A template for your own full‑stack “API + dashboard” projects