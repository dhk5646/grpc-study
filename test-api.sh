#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api/user"

# JSON 포맷팅 함수 (jq 없이)
format_json() {
    if command -v jq &> /dev/null; then
        jq '.'
    elif command -v python3 &> /dev/null; then
        python3 -m json.tool
    else
        cat
    fi
}

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  gRPC Client API 테스트${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 서버 확인
echo -e "${YELLOW}서버 상태 확인 중...${NC}"
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${RED}✗ 서버가 실행 중이지 않습니다${NC}"
    echo -e "${YELLOW}  ./start-all.sh 를 실행하여 서버를 시작하세요${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 서버 실행 중${NC}"
echo ""

# 1. 사용자 생성
echo -e "${GREEN}1. 사용자 생성${NC}"
echo -e "${YELLOW}   POST $BASE_URL${NC}"
RESPONSE=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "email": "hong@example.com",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678"
  }')

echo "$RESPONSE" | format_json
if command -v jq &> /dev/null; then
    USER_ID=$(echo "$RESPONSE" | jq -r '.id')
elif command -v python3 &> /dev/null; then
    USER_ID=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))")
else
    USER_ID="1"
fi
echo ""

# 2. 사용자 조회
echo -e "${GREEN}2. 사용자 조회${NC}"
echo -e "${YELLOW}   GET $BASE_URL/$USER_ID${NC}"
curl -s -X GET "$BASE_URL/$USER_ID" | format_json
echo ""

# 3. 사용자 목록 조회
echo -e "${GREEN}3. 사용자 목록 조회${NC}"
echo -e "${YELLOW}   GET $BASE_URL?page=0&size=10${NC}"
curl -s -X GET "$BASE_URL?page=0&size=10" | format_json
echo ""

# 4. 사용자 일괄 생성
echo -e "${GREEN}4. 사용자 일괄 생성${NC}"
echo -e "${YELLOW}   POST $BASE_URL/batch${NC}"
curl -s -X POST "$BASE_URL/batch" \
  -H "Content-Type: application/json" \
  -d '{
    "users": [
      {
        "email": "user1@example.com",
        "name": "사용자1",
        "phoneNumber": "010-1111-1111"
      },
      {
        "email": "user2@example.com",
        "name": "사용자2",
        "phoneNumber": "010-2222-2222"
      },
      {
        "email": "user3@example.com",
        "name": "사용자3",
        "phoneNumber": "010-3333-3333"
      }
    ]
  }' | format_json
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  테스트 완료! ✓${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}Tip: 더 예쁜 JSON 출력을 원하시면 jq를 설치하세요${NC}"
echo -e "${YELLOW}     brew install jq${NC}"
echo ""

