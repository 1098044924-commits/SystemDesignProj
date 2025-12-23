#!/bin/bash
set -euo pipefail

# CentOS setup script for Accounting app
# Usage: sudo bash setup-accounting-centos.sh

JAR_URL="${JAR_URL:-}"           # optional download URL
JAR_FILE="${JAR_FILE:-SystemDesignProj-1.0-SNAPSHOT.jar}"

if ! id -u accounting >/dev/null 2>&1; then
  useradd -m -s /bin/bash accounting
fi

mkdir -p /opt/accounting
chown accounting:accounting /opt/accounting
mkdir -p /etc/accounting
cat > /etc/accounting/accounting.env <<EOF
JDBC_DATABASE_URL=${JDBC_DATABASE_URL:-jdbc:h2:mem:accounting;DB_CLOSE_DELAY=-1}
JDBC_DATABASE_USERNAME=${JDBC_DATABASE_USERNAME:-sa}
JDBC_DATABASE_PASSWORD=${JDBC_DATABASE_PASSWORD:-}
JDBC_DATABASE_DRIVER=${JDBC_DATABASE_DRIVER:-org.h2.Driver}
JPA_HBM2DDL=${JPA_HBM2DDL:-update}
SPRING_PORT=${SPRING_PORT:-8080}
JAVA_OPTS="${JAVA_OPTS:- -Xms512m -Xmx1g}"
JAR_URL="${JAR_URL:-$JAR_URL}"
JAR_FILE="${JAR_FILE:-$JAR_FILE}"
EOF

# install java if missing
if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | grep "17" >/dev/null 2>&1; then
  if command -v dnf >/dev/null 2>&1; then
    dnf install -y java-17-openjdk-devel maven
  else
    yum install -y java-17-openjdk-devel maven
  fi
fi

# download jar if URL set and absent
if [ -n "${JAR_URL}" ] && [ ! -f "/opt/accounting/${JAR_FILE}" ]; then
  curl -fSL -o "/opt/accounting/${JAR_FILE}" "${JAR_URL}"
  chown accounting:accounting "/opt/accounting/${JAR_FILE}"
fi

cat > /etc/systemd/system/accounting.service <<'SERVICE'
[Unit]
Description=Accounting App
After=network.target

[Service]
Type=simple
User=accounting
Group=accounting
EnvironmentFile=/etc/accounting/accounting.env
WorkingDirectory=/opt/accounting
ExecStart=/usr/bin/java $JAVA_OPTS -jar /opt/accounting/${JAR_FILE}
Restart=on-failure
RestartSec=5s
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
SERVICE

systemctl daemon-reload
systemctl enable accounting

if [ -f "/opt/accounting/${JAR_FILE}" ]; then
  systemctl restart accounting
else
  echo "JAR not found at /opt/accounting/${JAR_FILE}. Upload it via scp or set JAR_URL and run again."
fi

echo "Setup finished. Check logs: sudo journalctl -u accounting -f"



