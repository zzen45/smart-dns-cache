# 🧠⚡ Smart DNS Cache (full‑stack demo)

___

Spin up a reactive Spring Boot + Redis DNS‑cache API **and** a lightweight Angular
dashboard with **one command** – no local Java/Node tooling required.

## 🚀 Quick start
___

```bash
git clone https://github.com/zzen45/smart-dns-cache.git
cd smart-dns-cache
docker compose up --build

Component	Exposed URL (after start‑up)
Front‑end (Angular + Nginx)	http://localhost/
REST API (Spring Boot)	http://localhost:8080/api/dns
Redis (transparent)	localhost:6379
Ctrl‑C to stop, or docker compose down -v to stop and remove volumes.



🖥️ Dashboard
----------------------------------------------------
Open http://localhost/ to:

View live cache stats
Add / delete records
Flush the cache



🔍 API cheatsheet
----------------------------------------------------
GET  /api/dns/cache              # list all cached records
POST /api/dns/cache              # add a manual record
DELETE /api/dns/cache/{domain}   # delete one record
DELETE /api/dns/cache            # clear everything
GET  /actuator/health            # system health
Example
curl http://localhost:8080/api/dns/cache | jq
curl -X POST http://localhost:8080/api/dns/cache \
     -H "Content-Type: application/json" \
     -d '{"domain":"example.com","ip":"93.184.216.34","ttl":300}'



⚙️ Project layout
----------------------------------------------------
smart-dns-cache/
├─ docker-compose.yml
├─ dns-cache-ui/     # Angular dashboard (compiled → Nginx)
└─ dnscache/         # Spring Boot API (Java 17) + Redis client
┌────────┐  HTTP (Docker net)  ┌─────────────┐  TCP  ┌────────┐
│ Angular│ ───────────────────►│ Spring Boot │──────►│ Redis  │
│  UI    │  localhost (80)     │  API :8080  │ :6379 │ Cache  │
└────────┘                     └─────────────┘       └────────┘



🛠️ Tweaks
----------------------------------------------------
Change ports – edit ports: in docker-compose.yml.
Skip the UI – comment‑out the frontend: service.
Change TTL / fallback servers – see dnscache/src/main/resources/application.yml.



🧹 Cleanup
----------------------------------------------------
docker compose down -v
docker image prune -f   # optional
Happy caching! 🎉