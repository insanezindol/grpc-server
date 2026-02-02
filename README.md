![header](https://capsule-render.vercel.app/api?type=wave&color=auto&height=300&section=header&text=grpc-server&fontSize=90)

# grpc-server

멤버 서버는 회원 정보(이름, 나이)를 관리하는 백엔드 서비스로, REST API와 gRPC API를 동시에 제공하는 하이브리드 서버입니다. MySQL 데이터베이스를 사용하여 회원 데이터를 저장하고 관리합니다.

---

## 주요 기능
- 이중 인터페이스: REST와 gRPC 프로토콜 동시 지원
- CRUD 연산: 회원 생성, 조회, 수정, 삭제 기능
- 데이터 영속성: MySQL 데이터베이스 연동
- 고성능 통신: gRPC를 통한 효율적인 데이터 전송

---

## 아키텍처 다이어그램
```text
┌─────────────────┐    REST 요청     ┌──────────────────┐
│  게이트웨이 서버 │ ───────────────> │                  │
│     (8081)      │                  │   멤버 서버      │
│                 │ <──────────────── │      (8082)      │
└─────────┬───────┘    REST 응답     └─────────┬────────┘
          │                                    │
          │           gRPC 요청/응답           │
          └────────────────────────────────────┘
                     (HTTP/2, 포트 9090)
```

---

## 프로젝트 구조
```text
grpc-server/
├── src/main/java/com/example/grpcserver/
│   ├── config/
│   │   └── GrpcServerConfig.java        # gRPC 서버 설정
│   ├── controller/
│   │   └── MemberRestController.java    # REST 컨트롤러
│   ├── dto/
│   │   ├── MemberRequest.java           # 요청 DTO
│   │   └── MemberResponse.java          # 응답 DTO
│   ├── entity/
│   │   └── Member.java                  # JPA 엔티티
│   ├── grpc/
│   │   └── GrpcMemberServiceImpl.java   # gRPC 서비스 구현
│   ├── repository/
│   │   └── MemberRepository.java        # JPA 레포지토리
│   ├── service/
│   │   ├── MemberService.java           # 서비스 구현체
│   └── MemberServiceApplication.java    # 메인 애플리케이션
├── src/main/proto/
│   └── member.proto                     # Protobuf 정의 파일
├── src/main/resources/
│   └── application.yml                  # 애플리케이션 설정
├── build.gradle                         # Gradle 빌드 파일
└── README.md                            # 프로젝트 문서
```

---

## 기술 스택

| 기술                             | 버전    | 용도            |
|--------------------------------|-------|---------------|
| Java                           | 21    | 프로그래밍 언어      |
| Spring Boot                    | 4.0.2 | 애플리케이션 프레임워크|
| Spring Data JPA                | 4.0.2 | 데이터 접근 계층|    
| Spring gRPC                    | 1.0.2 | 고성능 RPC 통신|
| MySQL                          | 8.0+  | 데이터베이스        |
| Lombok                         | -     | 코드 자동 생성      |
| Gradle                         | 8.5+  | 빌드 도구         |

---

## 설정 파일
- `application.yml`
```yaml
server:
  port: 8082  # REST API 포트

spring:
  application:
    name: member-service
  datasource:
    url: jdbc:mysql://localhost:3306/member_db
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

grpc:
  server:
    port: 9090           # gRPC 서버 포트
    enable-reflection: true  # gRPC 리플렉션 활성화
```

---

## API 엔드포인트
### REST API (포트 8082)

|메소드|엔드포인트|설명|요청 본문 예시|
|---|---|---|---|
|POST|/api/members|회원 생성|{"name":"홍길동","age":30}|
|GET|/api/members/{id}|회원 조회|-|
|GET|/api/members|모든 회원 조회|-|
|PUT|/api/members/{id}|회원 수정|{"name":"김철수","age":31}|
|DELETE|/api/members/{id}|회원 삭제|-|

### gRPC API (포트 9090)
|메소드|Protobuf서비스|설명|
|---|---|---|
|CreateMember|MemberService|회원 생성|
|GetMember|MemberService|회원 조회|
|UpdateMember|MemberService|회원 수정|
|DeleteMember|MemberService|회원 삭제|
|ListMembers|MemberService|모든 회원 조회|

---

## 실행 방법
### 1. 사전 요구사항
- Java 21 설치
- MySQL 8.0 이상 설치 및 실행
- Gradle 8.5 이상

### 2.프로젝트 빌드
```bash
# 프로젝트 디렉토리 이동
cd grpc-server

# 프로젝트 빌드
./gradlew build

# 또는 테스트 포함 빌드
./gradlew clean build
```
### 3. 애플리케이션 실행
```bash
# 방법 1: Gradle로 실행
./gradlew bootRun

# 방법 2: JAR 파일로 실행
java -jar build/libs/grpc-server-1.0.0.jar

# 방법 3: 특정 프로필로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```
### 4. 실행 확인
```bash
# REST API 확인
curl http://localhost:8082/api/members

# gRPC 서비스 확인 (grpcurl 필요)
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext localhost:9090 describe member.MemberService
```

---

## API 테스트 방법
### 1. REST API 테스트 (cURL)
```shell
# 회원 생성
curl -X POST http://localhost:8082/api/members \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","age":30}'

# 회원 조회
curl http://localhost:8082/api/members/1

# 모든 회원 조회
curl http://localhost:8082/api/members

# 회원 수정
curl -X PUT http://localhost:8082/api/members/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"김철수","age":31}'

# 회원 삭제
curl -X DELETE http://localhost:8082/api/members/1
```

### 2. gRPC API 테스트 (grpcurl)
```shell
# grpcurl 설치
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest

# 서비스 목록 확인
grpcurl -plaintext localhost:9090 list

# 서비스 정보 확인
grpcurl -plaintext localhost:9090 describe member.MemberService

# 회원 생성
grpcurl -plaintext -d '{"name":"이영희","age":25}' \
  localhost:9090 member.MemberService/CreateMember

# 회원 조회
grpcurl -plaintext -d '{"id":1}' \
  localhost:9090 member.MemberService/GetMember

# 모든 회원 조회
grpcurl -plaintext -d '{}' \
  localhost:9090 member.MemberService/ListMembers
```

---

## 개발 가이드
### 1. 새로운 API 추가 방법
#### 1.1 Protobuf 정의 추가
```proto
// member.proto 파일에 추가
service MemberService {
    // 기존 메소드들...
    rpc SearchMembers (SearchRequest) returns (SearchResponse);
}

message SearchRequest {
    string keyword = 1;
    int32 min_age = 2;
    int32 max_age = 3;
}

message SearchResponse {
    repeated MemberResponse members = 1;
    int32 total_count = 2;
}
```

#### 1.2 프로젝트 재빌드
```shell
./gradlew clean build
```

#### 1.3 gRPC 서비스 구현 추가
```java
public void searchMembers(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
    // 비즈니스 로직 구현
    List<Member> members = memberRepository.findByNameContainingAndAgeBetween(
        request.getKeyword(),
        request.getMinAge(),
        request.getMaxAge()
    );
    
    // 응답 생성 및 전송
}
```

#### 1.4 REST 컨트롤러 추가
```java
@GetMapping("/search")
public ResponseEntity<List<MemberResponse>> searchMembers(
    @RequestParam String keyword,
    @RequestParam(required = false) Integer minAge,
    @RequestParam(required = false) Integer maxAge) {
    
    // 비즈니스 로직 호출
    List<MemberResponse> result = memberService.searchMembers(keyword, minAge, maxAge);
    return ResponseEntity.ok(result);
}
```

---

## 문제 해결 가이드
### 일반적인 문제들
#### 1. 포트 충돌
```shell
# 8082 포트 사용 확인
netstat -an | grep 8082

# 9090 포트 사용 확인
netstat -an | grep 9090
```

#### 2. MySQL 연결 문제
```shell
# MySQL 서비스 상태 확인
sudo systemctl status mysql

# MySQL 접속 테스트
mysql -u root -p -h localhost -P 3306
```

#### 3. gRPC 연결 실패
```shell
# gRPC 서버 실행 확인
ps aux | grep grpc-server

# 네트워크 연결 테스트
telnet localhost 9090
```

#### 4. Protobuf 컴파일 오류
```shell
# Protobuf 파일 형식 확인
protoc --version

# gradle clean 후 재빌드
./gradlew clean build
```
