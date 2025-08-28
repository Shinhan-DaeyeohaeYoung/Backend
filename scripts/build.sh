#!/usr/bin/env bash
set -euo pipefail

# 프로젝트 루트로 이동
cd "$(dirname "$0")/.."

# Gradle 빌드 (테스트 스킵)
./gradlew clean build -x test

# buildx 준비
docker buildx ls >/dev/null 2>&1 || docker buildx create --use --name builder
docker buildx inspect --bootstrap >/dev/null

IMAGE="sso9594/daeyo-app"
TAG="${TAG:-latest}"
# 타깃 플랫폼: EC2(x86_64)면 linux/amd64, Graviton이면 linux/arm64
TARGET_PLATFORM="${TARGET_PLATFORM:-linux/amd64}"

# 단일 아키텍처로 빌드 & 푸시
docker buildx build \
  --platform "${TARGET_PLATFORM}" \
  -t "${IMAGE}:${TAG}" \
  --push .

echo "Pushed ${IMAGE}:${TAG} for ${TARGET_PLATFORM}"
