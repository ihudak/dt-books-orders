#!/bin/bash
./gradlew clean build
docker image build --platform linux/amd64 -t ihudak/dt-orders-service:latest .
docker push ihudak/dt-orders-service:latest

docker image build --platform linux/arm64 -t ihudak/dt-orders-service:arm64 .
docker push ihudak/dt-orders-service:arm64
