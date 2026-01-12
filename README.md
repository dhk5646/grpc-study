# 코드로 살펴보는 gRPC + Protobuf

> 실무 예제로 배우는 gRPC 마이크로서비스 아키텍처

---

## 📚 목차

### 1. 들어가며
- **1.1 왜 gRPC인가?**
    - REST API의 한계
    - HTTP/2 기반의 고성능 통신
    - 타입 안정성과 자동 코드 생성
    - 마이크로서비스 아키텍처에서의 이점

- **1.2 이 글에서 다룰 내용**
    - 멀티 모듈 프로젝트 구조 (Contract 패턴)
    - 4가지 gRPC 통신 방식 (Unary, Server/Client/Bidirectional Streaming)
    - Spring Boot + gRPC 통합
    - BFF 패턴 적용 (REST ↔ gRPC 게이트웨이)

---

### 2. Protobuf 기초 - IDL로 계약 정의하기

#### 2.1 Protobuf란?
```protobuf
syntax = "proto3";
```
- Protocol Buffers 소개
- JSON vs Protobuf (크기, 속도, 타입 안정성)
- 직렬화/역직렬화 원리

#### 2.2 메시지 정의
```protobuf
message CreateUserRequest {
  string email = 1;
  string name = 2;
  string phone_number = 3;
}
```
- 필드 번호의 의미 (하위 호환성)
- 네이밍 컨벤션 (snake_case vs camelCase)
- 기본 타입들 (string, int64, int32, bool)
- repeated 필드 (배열/리스트)

#### 2.3 서비스 정의
```protobuf
service UserService {
  rpc CreateUser (CreateUserRequest) returns (CreateUserResponse);
}
```
- RPC 메서드 정의
- Request/Response 네이밍 규칙
- Google API Style Guide 준수

#### 2.4 Java 코드 생성 옵션
```protobuf
option java_multiple_files = true;
option java_package = "com.study.grpc.proto";
```
- Gradle protobuf 플러그인 설정
- 생성되는 Java 클래스 구조
- Builder 패턴 활용

---

### 3. gRPC의 4가지 통신 방식

#### 3.1 Unary RPC - 단순 요청/응답
```protobuf
rpc CreateUser (CreateUserRequest) returns (CreateUserResponse);
```

**서버 구현:**
```java
public void createUser(CreateUserRequest request, 
                       StreamObserver<CreateUserResponse> responseObserver) {
    // 비즈니스 로직 처리
    responseObserver.onNext(response);
    responseObserver.onCompleted();
}
```

**클라이언트 구현:**
```java
CreateUserResponse response = blockingStub.createUser(request);
```

- BlockingStub vs AsyncStub 차이
- 동기/비동기 호출 방식
- 에러 처리 (Status, StatusRuntimeException)

#### 3.2 Server Streaming RPC - 하나 요청, 여러 응답
```protobuf
rpc GetUsers (GetUsersRequest) returns (stream GetUsersResponse);
```

**서버 구현:**
```java
public void getUsers(GetUsersRequest request, 
                     StreamObserver<GetUsersResponse> responseObserver) {
    for (User user : users) {
        responseObserver.onNext(toResponse(user));
    }
    responseObserver.onCompleted();
}
```

**클라이언트 구현:**
```java
blockingStub.getUsers(request)
    .forEachRemaining(response -> {
        // 각 응답 처리
    });
```

- 스트리밍의 이점 (대용량 데이터, 실시간 전송)
- 페이징 vs 스트리밍
- 백프레셔(Backpressure) 처리

#### 3.3 Client Streaming RPC - 여러 요청, 하나 응답
```protobuf
rpc BatchCreateUsers (stream CreateUserRequest) returns (BatchCreateUsersResponse);
```

**서버 구현:**
```java
public StreamObserver<CreateUserRequest> batchCreateUsers(
        StreamObserver<BatchCreateUsersResponse> responseObserver) {
    return new StreamObserver<>() {
        @Override
        public void onNext(CreateUserRequest request) {
            // 각 요청 처리
        }
        @Override
        public void onCompleted() {
            // 최종 응답 전송
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    };
}
```

**클라이언트 구현:**
```java
StreamObserver<CreateUserRequest> requestObserver = 
    asyncStub.batchCreateUsers(responseObserver);
    
for (CreateUserRequest request : requests) {
    requestObserver.onNext(request);
}
requestObserver.onCompleted();
```

- 일괄 처리 최적화
- CountDownLatch를 이용한 동기화
- 에러 처리 전략

#### 3.4 Bidirectional Streaming RPC - 양방향 스트리밍
```protobuf
rpc SyncUsers (stream SyncUsersRequest) returns (stream SyncUsersResponse);
```

**서버 구현:**
```java
public StreamObserver<SyncUsersRequest> syncUsers(
        StreamObserver<SyncUsersResponse> responseObserver) {
    return new StreamObserver<>() {
        @Override
        public void onNext(SyncUsersRequest request) {
            // 요청 받을 때마다 즉시 응답
            responseObserver.onNext(response);
        }
    };
}
```

- 실시간 양방향 통신 (채팅, 실시간 동기화)
- WebSocket vs gRPC Bidirectional Streaming
- 연결 관리 및 재연결 전략

---

### 4. 프로젝트 구조 - Contract 패턴

```
grpc-study/
├── grpc-contract/           # Protocol Buffers 정의
│   └── user.proto
├── grpc-server-app/         # gRPC 서버
│   └── UserGrpcService.java
└── grpc-client-app/         # gRPC 클라이언트 (BFF)
    └── UserGrpcClient.java
```

#### 4.1 왜 멀티 모듈인가?
- Contract 모듈의 독립성
- 버전 관리 전략
- 클라이언트/서버 간 계약 공유
- DRY 원칙 (Don't Repeat Yourself)

#### 4.2 grpc-contract 모듈
```groovy
// build.gradle
plugins {
    id 'com.google.protobuf'
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
    }
}
```

- Protobuf 플러그인 설정
- 코드 자동 생성 프로세스
- 생성된 클래스 구조 (Request, Response, ServiceGrpc)

#### 4.3 의존성 관리
```groovy
dependencies {
    implementation project(':grpc-contract')
}
```

- 버전 충돌 방지
- BOM (Bill of Materials) 활용
- gRPC 버전 통일 전략

---

### 5. Spring Boot + gRPC 통합

#### 5.1 gRPC 서버 구현

**서비스 정의:**
```java
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    private final UserService userService;
    
    @Override
    public void createUser(...) {
        // 구현
    }
}
```

- `@GrpcService` 어노테이션
- Spring Bean 주입 (의존성 주입)
- 비즈니스 로직 분리 (Service 레이어)

**설정:**
```yaml
grpc:
  server:
    port: 9090
```

- 포트 설정
- 인터셉터 추가
- 에러 핸들링

#### 5.2 gRPC 클라이언트 구현

**클라이언트 정의:**
```java
@Service
public class UserGrpcClient {
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub blockingStub;
    
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceStub asyncStub;
}
```

- `@GrpcClient` 어노테이션
- BlockingStub (동기)
- AsyncStub (비동기)

**설정:**
```yaml
grpc:
  client:
    user-service:
      address: 'static://localhost:9090'
      negotiationType: plaintext
```

- 주소 설정 (static, DNS)
- TLS vs Plaintext
- 연결 풀 관리

---

### 6. BFF 패턴 - REST ↔ gRPC 게이트웨이

```
[Browser] → HTTP/JSON → [Client App] → gRPC → [Server App]
```

#### 6.1 왜 BFF인가?
- 외부: REST API (브라우저 친화적)
- 내부: gRPC (고성능, 타입 안정성)
- 프로토콜 변환 계층

#### 6.2 REST Controller 구현
```java
@RestController
@RequestMapping("/api/user")
public class UserRestController {
    private final UserGrpcClient grpcClient;
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest request) {
        // REST → gRPC 변환
        CreateUserResponse grpcResponse = grpcClient.createUser(...);
        
        // gRPC → REST 변환
        return ResponseEntity.ok(toRestResponse(grpcResponse));
    }
}
```

- DTO 변환 (REST ↔ gRPC)
- 에러 응답 매핑
- HTTP 상태 코드 처리

#### 6.3 실무 고려사항
- 타임아웃 설정
- 재시도 정책 (Retry)
- Circuit Breaker
- 로깅 및 모니터링

---

### 7. 실전 예제 - 사용자 관리 API

#### 7.1 API 흐름
```
POST /api/user
  ↓
UserRestController
  ↓
UserGrpcClient (gRPC 호출)
  ↓
UserGrpcService (gRPC 서버)
  ↓
UserService (비즈니스 로직)
  ↓
UserRepository (DB)
```

#### 7.2 코드 실행
```bash
# 서버 시작
./start-all.sh

# API 테스트
curl -X POST http://localhost:8080/api/user \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","name":"테스트","phoneNumber":"010-1234-5678"}'
```

#### 7.3 로그 분석
```
REST API: 사용자 생성 요청 - email=test@test.com
  ↓
gRPC Client: Creating user - email=test@test.com
  ↓
gRPC CreateUser request: email=test@test.com
  ↓
gRPC CreateUser success: id=1
  ↓
gRPC Client: User created - id=1
  ↓
REST API: 사용자 생성 성공 - id=1
```

---

### 8. Protobuf 네이밍 Best Practices

#### 8.1 메시지 재사용
❌ **안 좋은 예:**
```protobuf
message CreateUserRequest { ... }
message BatchCreateUsersRequest { ... }  // 동일한 구조
```

✅ **좋은 예:**
```protobuf
message CreateUserRequest { ... }
rpc BatchCreateUsers (stream CreateUserRequest) returns (...);
```

#### 8.2 메서드별 Response 분리
❌ **안 좋은 예:**
```protobuf
rpc CreateUser (CreateUserRequest) returns (UserResponse);
rpc GetUser (GetUserRequest) returns (UserResponse);  // 공통 Response
```

✅ **좋은 예:**
```protobuf
rpc CreateUser (CreateUserRequest) returns (CreateUserResponse);
rpc GetUser (GetUserRequest) returns (GetUserResponse);
```

#### 8.3 Google API Style Guide
- `[MethodName]Request` / `[MethodName]Response` 패턴
- snake_case 필드명
- 명확한 서비스명 (xxxService)

---

### 9. 성능 최적화

#### 9.1 Protobuf vs JSON 비교
| 항목 | Protobuf | JSON |
|------|----------|------|
| 크기 | ~3-10배 작음 | 큼 |
| 속도 | ~5-10배 빠름 | 느림 |
| 가독성 | 낮음 | 높음 |
| 타입 안정성 | 강함 | 약함 |

#### 9.2 gRPC vs REST 비교
- HTTP/2 멀티플렉싱
- 헤더 압축 (HPACK)
- 바이너리 프레임
- 양방향 스트리밍

#### 9.3 실측 예제
```java
// 1000명 사용자 생성
// REST API: ~5초
// gRPC (일괄): ~0.5초 (10배 빠름)
```

---

### 10. 에러 처리 전략

#### 10.1 gRPC Status Codes
```java
try {
    userService.createUser(request);
} catch (Exception e) {
    responseObserver.onError(
        Status.INVALID_ARGUMENT
            .withDescription("Invalid email format")
            .asRuntimeException()
    );
}
```

- INVALID_ARGUMENT
- NOT_FOUND
- ALREADY_EXISTS
- PERMISSION_DENIED
- UNAVAILABLE

#### 10.2 클라이언트 에러 처리
```java
try {
    CreateUserResponse response = blockingStub.createUser(request);
} catch (StatusRuntimeException e) {
    if (e.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
        // 중복 처리
    }
}
```

#### 10.3 REST API 에러 매핑
```java
Status.NOT_FOUND → HTTP 404
Status.INVALID_ARGUMENT → HTTP 400
Status.PERMISSION_DENIED → HTTP 403
Status.INTERNAL → HTTP 500
```

---

### 11. 테스트 전략

#### 11.1 단위 테스트
```java
@Test
void testCreateUser() {
    CreateUserRequest request = CreateUserRequest.newBuilder()
        .setEmail("test@test.com")
        .build();
        
    CreateUserResponse response = service.createUser(request);
    
    assertThat(response.getEmail()).isEqualTo("test@test.com");
}
```

#### 11.2 통합 테스트
```java
@GrpcClientTest
class UserGrpcClientTest {
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub stub;
    
    @Test
    void testGrpcCall() {
        // 실제 gRPC 호출 테스트
    }
}
```

#### 11.3 Mock 서버
```java
@GrpcService
class MockUserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    @Override
    public void createUser(...) {
        // 테스트용 응답 반환
    }
}
```

---

### 12. 프로덕션 배포 고려사항

#### 12.1 보안
```yaml
grpc:
  client:
    user-service:
      negotiationType: TLS
      security:
        trustCertCollection: 'classpath:certs/ca.pem'
```

- TLS 암호화
- mTLS (상호 인증)
- 토큰 기반 인증 (JWT)

#### 12.2 모니터링
```java
@GrpcService(interceptors = MetricsInterceptor.class)
public class UserGrpcService { }
```

- gRPC 인터셉터
- Prometheus + Grafana
- 분산 추적 (OpenTelemetry)

#### 12.3 Kubernetes 배포
```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  ports:
  - port: 9090
    targetPort: 9090
    protocol: TCP
```

- Service Discovery (DNS)
- Load Balancing
- Health Check

#### 12.4 API Gateway 통합
- Envoy Proxy
- gRPC-Web (브라우저 직접 연결)
- Transcoding (REST ↔ gRPC)

---

### 13. 마이그레이션 가이드

#### 13.1 기존 REST API → gRPC
**단계별 전략:**
1. Contract 정의 (proto 파일)
2. gRPC 서버 구현 (기존 서비스 활용)
3. 클라이언트 점진적 마이그레이션
4. REST API 유지 (BFF 패턴)

#### 13.2 하위 호환성 유지
```protobuf
message UserResponse {
  int64 id = 1;
  string email = 2;
  // 새 필드 추가 (기존 클라이언트는 무시)
  string nickname = 3;
}
```

- 필드 번호 변경 금지
- 필드 제거 시 reserved 사용
- 버전별 서비스 운영

---

### 14. 실습 프로젝트 구조

```
grpc-study/
├── grpc-contract/
│   ├── build.gradle
│   └── src/main/proto/
│       └── user.proto
├── grpc-server-app/
│   ├── build.gradle
│   └── src/main/java/
│       └── com.study.grpc.user/
│           ├── grpc/
│           │   └── UserGrpcService.java
│           ├── service/
│           ├── repository/
│           └── domain/
└── grpc-client-app/
    ├── build.gradle
    └── src/main/java/
        └── com.study.grpc.client/
            ├── user/
            │   ├── UserGrpcClient.java
            │   └── controller/
            │       └── UserRestController.java
            └── GrpcClientApplication.java
```

#### 14.1 실행 방법
```bash
# 전체 빌드
./gradlew clean build

# 서버 시작
./start-all.sh

# API 테스트
./test-api.sh

# 서버 종료
./stop-all.sh
```

#### 14.2 API 예시
```bash
# 사용자 생성
curl -X POST http://localhost:8080/api/user \
  -d '{"email":"test@test.com","name":"테스트",...}'

# 사용자 조회
curl http://localhost:8080/api/user/1

# 사용자 목록
curl http://localhost:8080/api/user?page=0&size=10

# 일괄 생성
curl -X POST http://localhost:8080/api/user/batch \
  -d '{"users":[...]}'
```

---

### 15. 결론

#### 15.1 gRPC를 사용해야 하는 경우
✅ **적합:**
- 마이크로서비스 간 내부 통신
- 고성능이 중요한 경우
- 타입 안정성이 필요한 경우
- 스트리밍이 필요한 경우

❌ **부적합:**
- 브라우저 직접 연결 (gRPC-Web 필요)
- 레거시 시스템 통합
- 단순한 CRUD API

#### 15.2 핵심 요약
1. **Protobuf**: 효율적인 직렬화, 강타입, 하위 호환성
2. **4가지 RPC**: Unary, Server/Client/Bidirectional Streaming
3. **Contract 패턴**: 계약 분리, 코드 자동 생성
4. **BFF 패턴**: REST(외부) + gRPC(내부) 하이브리드
5. **Spring Boot 통합**: `@GrpcService`, `@GrpcClient`

#### 15.3 다음 단계
- gRPC 인터셉터 심화
- 분산 추적 (Tracing)
- Circuit Breaker 패턴
- gRPC-Gateway (REST Transcoding)
- Reactive gRPC (Reactor, RxJava)

---

### 📎 참고 자료

- [gRPC 공식 문서](https://grpc.io/)
- [Protocol Buffers](https://protobuf.dev/)
- [Google API Design Guide](https://cloud.google.com/apis/design)
- [grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter)
- [본 예제 GitHub](https://github.com/yourusername/grpc-study)

---

### 💬 Q&A

**Q: REST API를 완전히 대체해야 하나요?**
A: 아니요. 외부(REST) + 내부(gRPC) 하이브리드 구조가 현실적입니다.

**Q: 학습 곡선이 가파른가요?**
A: Protobuf 문법은 간단하지만, 스트리밍 구현은 복잡할 수 있습니다.

**Q: 성능 향상이 얼마나 되나요?**
A: 케이스별 다르지만, 평균 3-10배 빠르고 페이로드는 30-50% 감소합니다.

**Q: 기존 프로젝트에 적용하기 어렵나요?**
A: BFF 패턴으로 점진적 마이그레이션이 가능합니다.

---

## 🎯 이 글의 강점

1. **실무 예제**: 단순 Hello World가 아닌 실전 User 관리 시스템
2. **4가지 RPC 완전 구현**: 모든 통신 패턴 코드 제공
3. **멀티 모듈 구조**: Contract 분리, 실무 아키텍처
4. **BFF 패턴**: REST와 gRPC의 장점 결합
5. **실행 가능한 코드**: 복사/붙여넣기로 바로 실행
6. **Best Practices**: Google 스타일 가이드 준수

---

**태그**: #gRPC #Protobuf #마이크로서비스 #SpringBoot #HTTP2 #고성능통신 #BFF패턴

