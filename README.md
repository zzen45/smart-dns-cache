# 🔧 Smart DNS Cache — Full‑Stack Demo

A **reactive DNS caching micro‑service** with a minimal Angular dashboard.

* **Backend**: Spring Boot (WebFlux) + Redis  
* **Frontend**: Angular 17 served by Nginx  
* **Packaging**: Docker & Docker Compose

---

## 📦 Features

| Area      | What you get |
|-----------|--------------|
| **API**   | Resolve domains, cache with TTL, manual overrides, delete / clear cache |
| **Health**| `/actuator/health` with Redis, disk, SSL checks |
| **UI**    | View cache, add / delete / purge records, live stats |
| **DevOps**| One‑command spin‑up via `docker‑compose up --build` |

---

## 🚀 Quick Start

```bash
git clone https://github.com/<your‑github>/smart-dns-cache.git
cd smart-dns-cache
docker-compose up --build

Service	URL
Frontend	http://localhost:4200
Backend	http://localhost:8080
Redis	localhost:6379 (internal)
Stop everything:

docker-compose down -v            
🧪 Test the REST API (with Postman / curl)

# 1. View cached records
GET http://localhost:8080/api/dns/cache

# 2. Add a manual record
POST http://localhost:8080/api/dns/cache
Content-Type: application/json
{
  "domain": "example.local",
  "ip": "192.168.1.100",
  "ttl": 300
}

# 3. Delete a record
DELETE http://localhost:8080/api/dns/cache/example.local

# 4. Clear the entire cache
DELETE http://localhost:8080/api/dns/cache
Health check (verifies Redis connection):

curl http://localhost:8080/actuator/health
🖥️ Frontend Dashboard

Open http://localhost:4200 to:

🔄 Refresh & view current DNS cache
➕ Add manual records (domain / IP / TTL)
❌ Delete individual records
🧹 Clear the cache
✅ See system health badge
The UI is intentionally lightweight; feel free to extend it.
🗂️ Project Structure

smart-dns-cache/
│
├── docker-compose.yml
│
├── dns-cache-ui/          # Angular app
│   ├── Dockerfile
│   └── nginx.conf
│
└── dnscache/              # Spring Boot WebFlux service
    ├── Dockerfile
    └── src/main/java/...
🧑‍💻 Developer Workflow

Re‑build frontend only
cd dns-cache-ui
npm install               # first time
npm run build --prod
docker-compose up --build frontend
Re‑build backend only
cd dnscache
mvn clean package -DskipTests
docker-compose up --build dnscache
⚙️ Configuration Tweaks


Place	What to change
dnscache/src/main/resources/application.yml	Redis host/port, fallback resolvers, TTL
dns-cache-ui/src/environments/*	apiUrl if you expose backend elsewhere
docker-compose.yml	Port mapping, image tags, volumes