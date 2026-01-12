#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${RED}========================================${NC}"
echo -e "${RED}  gRPC 서비스 종료${NC}"
echo -e "${RED}========================================${NC}"
echo ""

# 9090 포트 (gRPC Server) 종료
if lsof -ti:9090 > /dev/null 2>&1; then
    echo -e "${YELLOW}9090 포트 (gRPC Server) 프로세스 종료 중...${NC}"
    lsof -ti:9090 | xargs kill -9 2>/dev/null
    echo -e "${GREEN}  ✓ gRPC Server 종료 완료${NC}"
else
    echo -e "${YELLOW}9090 포트에 실행 중인 프로세스 없음${NC}"
fi

# 8080 포트 (gRPC Client) 종료
if lsof -ti:8080 > /dev/null 2>&1; then
    echo -e "${YELLOW}8080 포트 (gRPC Client) 프로세스 종료 중...${NC}"
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    echo -e "${GREEN}  ✓ gRPC Client 종료 완료${NC}"
else
    echo -e "${YELLOW}8080 포트에 실행 중인 프로세스 없음${NC}"
fi

# Gradle 데몬 종료
echo ""
echo -e "${YELLOW}Gradle 데몬 종료 중...${NC}"
./gradlew --stop > /dev/null 2>&1
echo -e "${GREEN}  ✓ Gradle 데몬 종료 완료${NC}"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  모든 서비스 종료 완료! ✓${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

