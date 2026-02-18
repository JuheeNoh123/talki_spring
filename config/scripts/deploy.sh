#!/bin/bash
set -e

IMAGE="juheenoh/talki_spring:dev"

echo "===== deploy.sh start ====="
echo "Using image: $IMAGE"

docker --version

docker stop backend-server || true
docker rm backend-server || true

docker pull $IMAGE

docker run -d \
  --name backend-server \
  -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  --restart unless-stopped \
  $IMAGE

echo "===== deploy.sh end ====="
