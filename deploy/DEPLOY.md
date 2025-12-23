Ubuntu 22.04 step-by-step deployment (beginner-friendly)

Prerequisites
- A fresh Ubuntu 22.04 server with SSH access.
- A domain name pointed to the server (optional but recommended).
- sudo or root access.

1) Update system and install dependencies
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y openjdk-17-jdk maven nginx git mysql-server certbot python3-certbot-nginx
```

2) Create system user and folders
```bash
sudo useradd -r -s /bin/false accounting
sudo mkdir -p /opt/accounting-app
sudo chown $USER:$USER /opt/accounting-app
sudo mkdir -p /etc/accounting-app
sudo chown $USER:$USER /etc/accounting-app
```

3) Database (MySQL)
- Secure MySQL:
  sudo mysql_secure_installation
- Create DB and user:
```sql
CREATE DATABASE gnucash_like CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'appuser'@'%' IDENTIFIED BY 'change_this_password';
GRANT ALL PRIVILEGES ON gnucash_like.* TO 'appuser'@'%';
FLUSH PRIVILEGES;
```
- Edit `/etc/mysql/mysql.conf.d/mysqld.cnf` if you need external access (bind-address), then restart MySQL.

4) Copy repo and build
```bash
cd /opt/accounting-app
git clone <your-repo-url> .
mvn clean package -DskipTests
# the jar will be under target/
```

5) Put environment file and application config
- Copy `deploy/accounting.env` to `/etc/accounting-app/env` and edit passwords.
- Create `/etc/accounting-app/application-prod.yml` with any overrides if needed (or rely on env vars).

6) Install systemd unit
```bash
sudo cp deploy/accounting-app.service /etc/systemd/system/accounting-app.service
sudo chown root:root /etc/systemd/system/accounting-app.service
sudo systemctl daemon-reload
sudo systemctl enable accounting-app
sudo systemctl start accounting-app
sudo journalctl -u accounting-app -f
```

7) Configure Nginx
```bash
sudo cp deploy/nginx_accounting.conf /etc/nginx/sites-available/accounting
sudo ln -s /etc/nginx/sites-available/accounting /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

8) Obtain HTTPS (Let's Encrypt)
```bash
sudo certbot --nginx -d example.com
```

9) Validate app
- Visit http://example.com (or server IP) — check UI.
- API tests:
  - `curl http://127.0.0.1:8080/api/notifications/ping` -> should reply `pong`
  - Login via web UI and verify `/api/auth/me` returns JSON user
  - Check SSE: open browser console, you should see "SSE connected" and periodic "ping"/"connected" events

10) Backup & monitoring tips
- Set up `mysqldump` cron for DB backup.
- Ship logs to external system or set up `logrotate`.
- Monitor with simple health checks (curl to `/actuator/health` if enabled).

Troubleshooting
- If SSE shows connected then error: check Nginx proxy settings (buffering off, proxy_http_version 1.1, proxy_set_header Connection ""), and server logs.
- If requests return login HTML instead of JSON: ensure browser sends cookies (credentials included) and session is active; check proxy doesn't rewrite auth.
- If history shows empty: confirm transactions with `cleared=true` exist in DB.

If you want, I can:
- Create these files on the repo root (done) and add a simple startup script.
- Provide an Ansible playbook / bash script to automate all steps.




