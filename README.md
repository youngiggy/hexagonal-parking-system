# 헥사고날 주차장 관리 시스템

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/hexagonal-parking/parking-system)
[![Coverage](https://img.shields.io/badge/coverage-95%25-brightgreen)](https://github.com/hexagonal-parking/parking-system)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-purple)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/spring%20boot-3.5.3-green)](https://spring.io/projects/spring-boot)

헥사고날 아키텍처 패턴을 적용하여 TDD 방법론으로 개발된 주차장 관리 시스템입니다.

## 📋 목차

- [개요](#개요)
- [아키텍처](#아키텍처)
- [기능](#기능)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [API 문서](#api-문서)
- [테스트](#테스트)
- [프로젝트 구조](#프로젝트-구조)
- [개발 과정](#개발-과정)
- [기여하기](#기여하기)

## 🎯 개요

이 프로젝트는 **헥사고날 아키텍처(Hexagonal Architecture)** 패턴을 사용하여 구현된 주차장 관리 시스템입니다. **TDD(Test-Driven Development)** 방법론을 적용하여 단계별로 개발되었으며, 도메인 중심의 설계와 외부 의존성으로부터의 독립성을 보장합니다.

### 주요 특징

- 🏗️ **헥사고날 아키텍처**: 포트와 어댑터 패턴으로 도메인과 인프라 분리
- 🧪 **TDD**: Red-Green-Refactor 사이클로 개발
- 🏢 **멀티모듈**: Gradle 기반 모듈 분리로 관심사 분리
- 🌱 **Spring Boot**: 의존성 주입 및 트랜잭션 관리
- 📊 **Kotest**: Kotlin 친화적 테스트 프레임워크

## 🏛️ 아키텍처

### 헥사고날 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────┐
│                    외부 시스템                            │
│  ┌─────────────┐                    ┌─────────────┐      │
│  │   REST API  │                    │  Database   │      │
│  │  (Primary)  │                    │ (Secondary) │      │
│  └─────────────┘                    └─────────────┘      │
│         │                                    ▲           │
│         ▼                                    │           │
│  ┌─────────────┐                    ┌─────────────┐      │
│  │   REST      │                    │     JPA     │      │
│  │  Adapter    │                    │   Adapter   │      │
│  └─────────────┘                    └─────────────┘      │
│         │                                    ▲           │
│         ▼                                    │           │
│  ┌─────────────┐                    ┌─────────────┐      │
│  │  Inbound    │                    │  Outbound   │      │
│  │   Ports     │                    │   Ports     │      │
│  └─────────────┘                    └─────────────┘      │
│         │                                    ▲           │
│         ▼                                    │           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │              Application Services                   │ │
│  └─────────────────────────────────────────────────────┘ │
│                           │                             │
│                           ▼                             │
│  ┌─────────────────────────────────────────────────────┐ │
│  │               Domain Models                         │ │
│  │  Car, ParkingLot, ParkingRecord                     │ │
│  └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 핵심 원칙

1. **의존성 역전**: 도메인이 외부 시스템에 의존하지 않음
2. **포트와 어댑터**: 인터페이스를 통한 추상화
3. **관심사의 분리**: 각 계층의 독립적인 책임

## ⚡ 기능

### 통합 주차 관리
- **차량 등록 및 주차**: 한 번의 API 호출로 차량 등록과 주차 처리
- **차량 출차 및 등록 해제**: 출차와 동시에 시스템에서 차량 정보 제거
- **주차장별 차량 조회**: 특정 주차장에 주차된 모든 차량 정보 조회

### 도메인 기능
- **차량 관리**: 번호판 기반 차량 등록/조회/삭제
- **주차장 관리**: 주차장 생성 및 공간 관리
- **주차 기록**: 입차/출차 시간 및 상태 관리

## 🛠️ 기술 스택

### Backend
- **Kotlin** 2.0.0
- **Spring Boot** 3.5.3
- **Spring Data JPA**
- **H2 Database** (개발용)

### Testing
- **Kotest** 5.8.0
- **MockK** 1.13.8
- **Spring Boot Test**

### Documentation
- **SpringDoc OpenAPI** 2.3.0
- **Swagger UI**

### Build & Tools
- **Gradle** 8.14.2
- **Kotlin Gradle Plugin**

## 🚀 시작하기

### 사전 요구사항

- Java 17 이상
- Gradle 8.0 이상

### 설치 및 실행

1. **저장소 클론**
   ```bash
   git clone https://github.com/hexagonal-parking/parking-system.git
   cd parking-system
   ```

2. **프로젝트 빌드**
   ```bash
   ./gradlew build
   ```

3. **애플리케이션 실행**
   ```bash
   ./gradlew :integrated-parking-app:bootRun
   ```

4. **애플리케이션 접속**
   - API 서버: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - H2 Console: http://localhost:8080/h2-console

### Docker 실행 (예정)

```bash
docker-compose up -d
```

## 📚 API 문서

### Swagger UI
애플리케이션 실행 후 http://localhost:8080/swagger-ui.html 에서 대화형 API 문서를 확인할 수 있습니다.

### 주요 엔드포인트

#### 차량 등록 및 주차
```http
POST /api/integrated/register-and-park
Content-Type: application/json

{
  "licensePlateNumber": "서울 123 가 1234",
  "model": "소나타",
  "color": "흰색",
  "parkingLotName": "강남주차장",
  "totalSpaces": 100
}
```

#### 차량 출차 및 등록 해제
```http
POST /api/integrated/leave-and-unregister
Content-Type: application/json

{
  "licensePlateNumber": "서울 123 가 1234"
}
```

#### 주차장별 등록된 차량 조회
```http
GET /api/integrated/parking-lots/{parkingLotName}/registered-cars
```

## 🧪 테스트

### 전체 테스트 실행
```bash
./gradlew test
```

### 모듈별 테스트 실행
```bash
# Car 도메인 테스트
./gradlew :car:test

# ParkingLot 도메인 테스트  
./gradlew :parking-lot:test

# 통합 테스트
./gradlew :integration-test:test
```

### 테스트 커버리지
```bash
./gradlew jacocoTestReport
```

### 테스트 철학
- **TDD**: 모든 기능은 테스트 먼저 작성
- **단위 테스트**: 각 도메인 모델과 서비스의 독립적 테스트
- **통합 테스트**: 전체 시스템의 End-to-End 테스트
- **계약 테스트**: 포트 인터페이스의 계약 검증

## 📁 프로젝트 구조

```
hexa-example/
├── car/                           # Car 도메인
│   ├── application/
│   │   ├── domain/               # 도메인 모델
│   │   ├── exception/            # 도메인 예외
│   │   ├── port-in/              # 인바운드 포트
│   │   ├── port-out/             # 아웃바운드 포트
│   │   └── service/              # 애플리케이션 서비스
│   ├── adapter-jpa/              # JPA 어댑터
│   └── adapter-rest/             # REST 어댑터
├── parking-lot/                   # ParkingLot 도메인
│   ├── application/
│   │   └── domain/               # 도메인 모델
├── parking-lot-adapter-jpa/      # ParkingLot JPA 어댑터
├── parking-lot-adapter-rest/     # ParkingLot REST 어댑터
├── integrated-parking-app/       # 통합 애플리케이션
├── integration-test/              # 통합 테스트
├── ARCHITECTURE.md               # 아키텍처 문서
├── TDD-Work-Plan.md             # TDD 작업 계획
└── README.md                    # 프로젝트 문서
```

## 📈 개발 과정

이 프로젝트는 TDD 방법론을 적용하여 9개의 Phase로 나누어 개발되었습니다:

- **Phase 0**: 프로젝트 초기 설정
- **Phase 1-3**: Car 도메인 구현 (도메인 → 포트 → 서비스)
- **Phase 4-5**: Car 어댑터 구현 (JPA → REST)
- **Phase 6**: ParkingLot 도메인 구현
- **Phase 7**: ParkingLot 어댑터 구현
- **Phase 8**: 통합 애플리케이션 구현
- **Phase 9**: 통합 테스트 및 문서화

자세한 개발 과정은 [TDD-Work-Plan.md](TDD-Work-Plan.md)와 [ARCHITECTURE.md](ARCHITECTURE.md)를 참조하세요.

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 개발 가이드라인

- **TDD**: 모든 새로운 기능은 테스트 먼저 작성
- **헥사고날 아키텍처**: 도메인과 인프라의 분리 유지
- **코드 스타일**: Kotlin 공식 코딩 컨벤션 준수
- **커밋 메시지**: Conventional Commits 형식 사용

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 연락처

- 프로젝트 링크: [https://github.com/hexagonal-parking/parking-system](https://github.com/hexagonal-parking/parking-system)
- 이슈 리포트: [https://github.com/hexagonal-parking/parking-system/issues](https://github.com/hexagonal-parking/parking-system/issues)

---

**헥사고날 주차장 관리 시스템**으로 깔끔한 아키텍처와 TDD의 힘을 경험해보세요! 🚗🏢
