#!/bin/bash
echo ""
echo -e "  또는 ${YELLOW}kill $SERVER_PID $CLIENT_PID${NC}"
echo -e "  ${YELLOW}./stop-all.sh${NC}"
echo -e "종료:"
echo ""
echo -e "    ${YELLOW}-d '{\"email\":\"test@test.com\",\"name\":\"테스트\",\"phoneNumber\":\"010-1234-5678\"}'${NC}"
echo -e "    ${YELLOW}-H \"Content-Type: application/json\" \\${NC}"
echo -e "  ${YELLOW}curl -X POST http://localhost:8080/api/user \\${NC}"
echo -e "테스트:"
echo ""
echo -e "  - Client: ${YELLOW}tail -f /tmp/grpc-client.log${NC}"
echo -e "  - Server: ${YELLOW}tail -f /tmp/grpc-server.log${NC}"
echo -e "로그 확인:"
echo ""
echo -e "  - gRPC Client:  http://localhost:8080 (PID: $CLIENT_PID)"
echo -e "  - gRPC Server:  http://localhost:9090 (PID: $SERVER_PID)"
echo -e "서비스 정보:"
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  모든 서비스 시작 완료! ✓${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

fi
    exit 1
    kill $SERVER_PID 2>/dev/null
    echo -e "${RED}   로그 확인: tail -f /tmp/grpc-client.log${NC}"
    echo -e "${RED}   ✗ 클라이언트 시작 실패${NC}"
else
    echo -e "${GREEN}   ✓ 클라이언트 시작 완료${NC}"
if ps -p $CLIENT_PID > /dev/null; then
# 클라이언트가 정상 시작되었는지 확인

sleep 5
echo -e "${YELLOW}   클라이언트 시작 대기 중... (5초)${NC}"

echo "   PID: $CLIENT_PID"
CLIENT_PID=$!
./gradlew :grpc-client-app:bootRun > /tmp/grpc-client.log 2>&1 &
echo -e "${GREEN}2. gRPC Client (BFF) 시작 (포트: 8080)${NC}"
echo ""

fi
    exit 1
    echo -e "${RED}   로그 확인: tail -f /tmp/grpc-server.log${NC}"
    echo -e "${RED}   ✗ 서버 시작 실패${NC}"
else
    echo -e "${GREEN}   ✓ 서버 시작 완료${NC}"
if ps -p $SERVER_PID > /dev/null; then
# 서버가 정상 시작되었는지 확인

sleep 5
echo -e "${YELLOW}   서버 시작 대기 중... (5초)${NC}"

echo "   PID: $SERVER_PID"
SERVER_PID=$!
./gradlew :grpc-server-app:bootRun > /tmp/grpc-server.log 2>&1 &
echo -e "${GREEN}1. gRPC Server 시작 (포트: 9090)${NC}"
echo ""

fi
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    echo -e "${RED}8080 포트 사용 중인 프로세스 종료${NC}"
if lsof -ti:8080 > /dev/null 2>&1; then

fi
    lsof -ti:9090 | xargs kill -9 2>/dev/null
    echo -e "${RED}9090 포트 사용 중인 프로세스 종료${NC}"
if lsof -ti:9090 > /dev/null 2>&1; then
echo -e "${YELLOW}기존 프로세스 확인 중...${NC}"
# 기존 프로세스 종료

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  gRPC 서비스 시작${NC}"
echo -e "${GREEN}========================================${NC}"

NC='\033[0m' # No Color
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
# 색상 정의


