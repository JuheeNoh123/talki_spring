#!/bin/bash
set -e

echo "===== deploy.sh start ====="

docker --version

docker stop backend-server || true
docker rm backend-server || true

docker pull juheenoh123/talki_spring-dev:dev

docker run -d \
  --name backend-server \
  -p 8080:8080 \
  --restart unless-stopped \
  juheenoh123/talki_spring-dev:dev

echo "===== deploy.sh end ====="
