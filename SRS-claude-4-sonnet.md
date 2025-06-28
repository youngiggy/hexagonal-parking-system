# 소프트웨어 요구사항 명세서 (SRS)
# 헥사고날 주차장 시스템 (Hexagonal Parking System)

## 1. 소개

### 1.1 목적
이 문서는 헥사고날 아키텍처(Hexagonal Architecture)를 적용한 주차장 관리 시스템의 요구사항을 정의합니다. 이 프로젝트는 TDD(Test-Driven Development) 방법론을 사용하여 단계별로 구현될 예정입니다.

### 1.2 프로젝트 범위
이 프로젝트는 차량(Car)과 주차장(ParkingLot) 두 도메인을 중심으로 구성된 주차장 관리 시스템입니다. 헥사고날 아키텍처의 핵심 개념인 도메인 모델의 순수성 유지, 포트와 어댑터를 통한 내부와 외부의 명확한 분리, 도메인 중심의 의존성 방향을 보여주는 교육용 예제입니다.

### 1.3 기술 스택
- 언어: Kotlin
- 프레임워크: Spring Boot 3.5.0
- 빌드 도구: Gradle (Kotlin DSL)
- 테스트 프레임워크: Kotest, MockK
- 데이터베이스: JPA/Hibernate
- API 문서화: SpringDoc OpenAPI (Swagger)
- Java 버전: 21

## 2. 시스템 개요

### 2.1 시스템 아키텍처
이 프로젝트는 헥사고날 아키텍처(포트 및 어댑터 아키텍처)를 따릅니다. 시스템은 다음과 같은 주요 구성 요소로 이루어집니다:

1. **도메인 계층 (Domain Layer)**
   - Car 도메인: 차량 정보 및 번호판 관리
   - ParkingLot 도메인: 주차장 운영 및 주차 이벤트 관리
   - 비즈니스 로직과 규칙을 포함하며 외부 의존성이 없음

2. **포트 (Ports)**
   - 인바운드 포트 (Port-In): 유스케이스 인터페이스
   - 아웃바운드 포트 (Port-Out): 외부 시스템과의 통신 인터페이스

3. **어댑터 (Adapters)**
   - 인바운드 어댑터: REST API, 스케줄러
   - 아웃바운드 어댑터: JPA 리포지토리

### 2.2 모듈 구조
프로젝트는 다음과 같은 모듈로 구성됩니다:

```
kotlin/
├── application-api/           # API 애플리케이션 부트스트랩
├── application-cron/          # 배치/스케줄러 애플리케이션
├── bootstrap/                 # 공통 부트스트랩 설정
├── car/                       # 차량 도메인
│   ├── adapter-jpa/          # JPA 어댑터
│   ├── adapter-rest/         # REST API 어댑터
│   └── application/          # 애플리케이션 계층
│       ├── domain/           # 도메인 모델
│       ├── exception/        # 도메인 예외
│       ├── port-in/          # 인바운드 포트
│       ├── port-out/         # 아웃바운드 포트
│       └── service/          # 유스케이스 구현
└── parking-lot/              # 주차장 도메인
    ├── adapter-cron/         # 스케줄러 어댑터
    ├── adapter-rest/         # REST API 어댑터
    └── application/          # 애플리케이션 계층
        ├── domain/           # 도메인 모델
        ├── port-in/          # 인바운드 포트
        └── service/          # 유스케이스 구현
```

## 3. 기능 요구사항

### 3.1 차량(Car) 도메인

#### 3.1.1 도메인 모델
- **CarEntity**: 차량 엔티티
  - 속성: identity (UUID), licensePlateNumber, createdAt, updatedAt
- **LicensePlateNumber**: 번호판 값 객체
  - 한국 자동차 번호판 형식 검증 (`[가-힣]{0,2}\\s?[0-9]{1,3}\\s?[가-힣]\\s?[0-9]{4}`)

#### 3.1.2 유스케이스
1. **차량 등록 (CarCommandUseCase)**
   - 여러 차량을 일괄 등록
   - 번호판 형식 검증
   
2. **차량 조회 (CarQueryUseCase)**
   - 번호판으로 차량 조회
   - 등록되지 않은 차량 조회 시 CarNotFoundException 발생

#### 3.1.3 비즈니스 규칙
- 번호판 번호는 한국 자동차 번호판 형식을 준수해야 함
- 동일한 번호판의 차량은 중복 등록할 수 없음

### 3.2 주차장(ParkingLot) 도메인

#### 3.2.1 도메인 모델
- **ParkingLot**: 주차장 엔티티
  - 주차 이벤트 관리 (입차/출차)
  - 동시성 안전을 위한 동기화된 컬렉션 사용

- **ParkingEvent**: 주차 이벤트
  - 속성: car, feePolicy, enteredAt, leavedAt
  - 주차 시간 계산 (parkingDuration)
  - 주차 요금 계산 (parkingFee)

- **ParkingFeePolicy**: 주차 요금 정책
  - 시간 기반 요금 계산 로직

- **Money**: 금액 값 객체
  - 주차 요금 표현

#### 3.2.2 유스케이스
1. **입차 처리**
   - 차량 입차 등록
   - 주차 요금 정책 적용
   - 입차 시간 기록

2. **출차 처리**
   - 차량 출차 처리
   - 주차 시간 및 요금 계산
   - 입차 기록이 없는 경우 예외 처리

3. **정산 처리 (스케줄러)**
   - 주기적인 주차 요금 정산
   - 배치 작업으로 실행

#### 3.2.3 비즈니스 규칙
- 입차 기록이 없는 차량은 출차할 수 없음
- 주차 요금은 주차 시간과 요금 정책에 따라 계산됨
- 동일한 차량의 중복 입차는 허용하지 않음

### 3.3 API 요구사항

#### 3.3.1 차량 관리 API
- `POST /cars`: 차량 일괄 등록
- `GET /cars?licensePlateNumber={번호판}`: 차량 조회

#### 3.3.2 주차장 관리 API
- `POST /parking-lots/enter`: 차량 입차
- `POST /parking-lots/leave`: 차량 출차
- `GET /parking-lots/events`: 주차 이벤트 조회

## 4. 비기능 요구사항

### 4.1 성능
- API 응답 시간: 평균 200ms 이내
- 동시 사용자: 최소 100명 지원
- 주차장 동시성: 여러 차량의 동시 입출차 처리

### 4.2 보안
- 입력 데이터 검증 (Bean Validation)
- SQL 인젝션 방지 (JPA 사용)
- 적절한 HTTP 상태 코드 반환

### 4.3 확장성
- 모듈화된 구조로 독립적인 배포 가능
- 마이크로서비스 아키텍처로의 전환 용이성
- 새로운 어댑터 추가 용이성

### 4.4 유지보수성
- 헥사고날 아키텍처를 통한 관심사 분리
- 도메인 로직의 외부 의존성 제거
- 명확한 인터페이스 정의 (포트)

### 4.5 테스트 가능성
- 도메인 로직의 단위 테스트 용이성
- 포트를 통한 모킹 가능
- TDD 방법론 적용

## 5. 인터페이스 요구사항

### 5.1 REST API
- JSON 형식의 요청/응답
- RESTful 설계 원칙 준수
- OpenAPI 3.0 스펙 문서화 (Swagger UI)
- 표준 HTTP 상태 코드 사용

### 5.2 데이터베이스
- JPA/Hibernate를 통한 ORM
- H2 인메모리 데이터베이스 (개발/테스트)
- 트랜잭션 관리

### 5.3 스케줄러
- Spring의 @Scheduled 어노테이션 사용
- 주기적인 정산 작업 실행

## 6. 개발 접근 방식

### 6.1 TDD 방법론
1. **Red**: 실패하는 테스트 작성
2. **Green**: 테스트를 통과하는 최소한의 코드 작성
3. **Refactor**: 코드 개선 및 리팩토링
4. 반복

### 6.2 헥사고날 아키텍처 구현 단계
1. **도메인 모델 정의**: 순수한 비즈니스 로직 구현
2. **포트 정의**: 인바운드/아웃바운드 인터페이스 정의
3. **서비스 구현**: 유스케이스 구현
4. **어댑터 구현**: 외부 시스템과의 연동
5. **통합**: 전체 시스템 조립

### 6.3 개발 단계
1. **1단계**: Car 도메인 모델 및 비즈니스 규칙 구현
2. **2단계**: Car 유스케이스 및 포트 정의
3. **3단계**: Car JPA 어댑터 구현
4. **4단계**: Car REST 어댑터 구현
5. **5단계**: ParkingLot 도메인 모델 구현
6. **6단계**: ParkingLot 유스케이스 구현
7. **7단계**: ParkingLot 어댑터 구현
8. **8단계**: 애플리케이션 부트스트랩 구현
9. **9단계**: 통합 테스트 및 시스템 테스트

## 7. 테스트 전략

### 7.1 테스트 수준
1. **단위 테스트**: 도메인 모델, 서비스 로직
2. **통합 테스트**: 어댑터와 외부 시스템 연동
3. **시스템 테스트**: 전체 API 기능 테스트

### 7.2 테스트 도구
- **Kotest**: Kotlin 친화적 테스트 프레임워크
- **MockK**: Kotlin 모킹 라이브러리
- **Spring Boot Test**: 스프링 통합 테스트
- **TestContainers**: 데이터베이스 통합 테스트 (필요시)

### 7.3 테스트 커버리지
- 도메인 로직: 100% 커버리지 목표
- 서비스 계층: 90% 이상 커버리지
- 전체 시스템: 80% 이상 커버리지

## 8. 예외 처리

### 8.1 도메인 예외
- **CarNotFoundException**: 등록되지 않은 차량 조회 시
- **IllegalArgumentException**: 잘못된 번호판 형식
- **RuntimeException**: 기타 비즈니스 규칙 위반

### 8.2 API 예외 처리
- 400 Bad Request: 잘못된 요청 데이터
- 404 Not Found: 리소스를 찾을 수 없음
- 500 Internal Server Error: 서버 내부 오류

## 9. 배포 및 운영

### 9.1 애플리케이션 구성
- **application-api**: REST API 서버
- **application-cron**: 배치/스케줄러 서버

### 9.2 컨테이너화
- Docker 이미지 빌드 (Jib 플러그인)
- 독립적인 컨테이너 실행 가능

### 9.3 모니터링
- 애플리케이션 로그 (kotlin-logging)
- 스케줄러 실행 로그
- API 요청/응답 로그

## 10. 데이터 모델

### 10.1 Car 엔티티
```kotlin
data class CarEntity(
    val identity: CarIdentity,           // UUID
    val licensePlateNumber: LicensePlateNumber,  // 번호판
    val createdAt: Instant,             // 생성일시
    val updatedAt: Instant              // 수정일시
)
```

### 10.2 ParkingEvent 엔티티
```kotlin
data class ParkingEvent(
    val car: CarModel,                  // 차량 정보
    val feePolicy: ParkingFeePolicy,    // 요금 정책
    val enteredAt: Instant,             // 입차 시간
    val leavedAt: Instant?              // 출차 시간 (nullable)
)
```

## 11. 결론

이 SRS 문서는 헥사고날 아키텍처를 적용한 주차장 관리 시스템의 요구사항을 정의합니다. 이 프로젝트는 TDD 방법론을 사용하여 개발되며, 도메인 모델의 순수성을 유지하고 포트와 어댑터 패턴을 통해 유연하고 테스트 가능한 코드를 작성하는 것을 목표로 합니다.

헥사고날 아키텍처의 핵심 원칙인 도메인 중심 설계, 의존성 역전, 관심사 분리를 통해 유지보수가 용이하고 확장 가능한 시스템을 구축할 것입니다.
