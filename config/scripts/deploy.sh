#!/bin/bash
set -e

echo "===== deploy.sh start (Ubuntu) ====="

# 1. Docker 설치 (Ubuntu)
if ! command -v docker &> /dev/null; then
  echo "Docker not found. Installing Docker..."

  sudo apt-get update -y
  sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

  sudo apt-get install -y docker.io
  sudo systemctl start docker
  sudo systemctl enable docker
fi

# 2. docker 그룹 권한 (CI에서도 즉시 적용)
sudo usermod -aG docker $USER
newgrp docker <<EOF
echo "Docker group applied"
EOF

# 3. Docker 동작 확인
docker --version

echo "===== deploy.sh end ====="
