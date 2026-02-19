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
  --network host \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e MYSQL_PASSWORD=$MYSQL_PASSWORD \
  -e REDIS_PASSWORD=$REDIS_PASSWORD \
  --restart unless-stopped \
  $IMAGE

echo "===== deploy.sh end ====="
