# 헥사고날 주차장 시스템 아키텍처

## 📋 목차
- [개요](#개요)
- [헥사고날 아키텍처 원칙](#헥사고날-아키텍처-원칙)
- [현재 구현 상태](#현재-구현-상태)
- [모듈 구조](#모듈-구조)
- [아키텍처 다이어그램](#아키텍처-다이어그램)
- [구현 진행 상황](#구현-진행-상황)

## 개요

이 프로젝트는 **헥사고날 아키텍처(Hexagonal Architecture)** 패턴을 사용하여 구현된 주차장 관리 시스템입니다.
**TDD(Test-Driven Development)** 방법론을 적용하여 단계별로 개발되고 있습니다.

### 주요 특징
- 🏗️ **헥사고날 아키텍처**: 포트와 어댑터 패턴
- 🧪 **TDD**: Red-Green-Refactor 사이클
- 🏢 **멀티모듈**: Gradle 기반 모듈 분리
- 🌱 **Spring Boot**: 의존성 주입 및 트랜잭션 관리
- 📊 **Kotest**: Kotlin 테스트 프레임워크

## 헥사고날 아키텍처 원칙

### 1. 의존성 역전 (Dependency Inversion)
- 도메인이 외부 시스템에 의존하지 않음
- 인터페이스(포트)를 통한 추상화

### 2. 포트와 어댑터 (Ports & Adapters)
- **인바운드 포트**: 애플리케이션으로 들어오는 요청
- **아웃바운드 포트**: 애플리케이션에서 나가는 요청
- **어댑터**: 포트의 구체적인 구현체

### 3. 관심사의 분리 (Separation of Concerns)
- 도메인 로직과 기술적 세부사항 분리
- 각 계층의 독립적인 테스트 가능

## 현재 구현 상태

### ✅ 완료된 Phase

#### Phase 0: 프로젝트 초기 설정
- 멀티모듈 Gradle 프로젝트 구조
- 테스트 환경 설정 (Kotest, MockK)

#### Phase 1: Car 도메인 구현
- `LicensePlateNumber`: 번호판 값 객체
- `Car*`: 도메인 모델 (CarEntity, CarData, CarKey)
- `CarNotFoundException`: 도메인 예외

#### Phase 2: Car 포트 정의
- **인바운드 포트**: `CarQueryUseCase`, `CarCommandUseCase`
- **아웃바운드 포트**: `CarLoadPort`, `CarSavePort`

#### Phase 3: Car 서비스 구현
- `CarService`: 비즈니스 로직 구현
- 인바운드 포트 구현 + 아웃바운드 포트 사용
- 트랜잭션 관리 (`@Transactional`)

#### Phase 4: Car JPA 어댑터 구현
- `CarJpaEntity`: JPA 엔티티 매핑
- `CarJpaRepository`: Spring Data JPA 리포지토리
- `CarJpaAdapter`: 아웃바운드 포트 구현

#### Phase 5: Car REST 어댑터 구현
- `CarRequestDto`: 요청 데이터 전송 객체
- `CarResponseDto`: 응답 데이터 전송 객체
- `CarRestApi`: REST 컨트롤러

#### Phase 6: ParkingLot 도메인 구현
- `ParkingLot`: 주차장 도메인 모델
- `ParkingRecord`: 주차 기록 도메인 모델
- `ParkingStatus`: 주차 상태 열거형
- `ParkingLotService`: 비즈니스 로직 구현
- **참고**: ParkingLot은 현재 단순한 구조 (domain만 존재)

#### Phase 7: ParkingLot 어댑터 구현
- `ParkingLotJpaAdapter`: JPA 어댑터 구현
- `ParkingLotRestApi`: REST API 구현
- 통합 테스트 완료

#### Phase 8: 통합 애플리케이션 구현
- `integrated-parking-app`: 통합 애플리케이션 모듈
- Car와 ParkingLot 도메인 통합
- Spring Boot 애플리케이션 구성

### 🚧 진행 예정 Phase

## 모듈 구조

```
hexagonal-parking-system/
├── car/                           # Car 도메인
│   ├── application/
│   │   ├── domain/               # ✅ 도메인 모델
│   │   ├── exception/            # ✅ 도메인 예외
│   │   ├── port-in/              # ✅ 인바운드 포트
│   │   ├── port-out/             # ✅ 아웃바운드 포트
│   │   └── service/              # ✅ 애플리케이션 서비스
│   ├── adapter-jpa/              # ✅ JPA 어댑터
│   └── adapter-rest/             # ✅ REST 어댑터
├── parking-lot/                   # ParkingLot 도메인
│   └── application/
│       └── domain/               # ✅ 도메인 모델
├── parking-lot-adapter-jpa/      # ✅ ParkingLot JPA 어댑터
├── parking-lot-adapter-rest/     # ✅ ParkingLot REST 어댑터
├── parking-lot-adapter-integration-test/ # ✅ 통합 테스트
├── integrated-parking-app/       # ✅ 통합 애플리케이션
├── integration-test/              # ✅ 통합 테스트
├── application-api/               # ⏳ API 애플리케이션 (예정)
├── application-cron/              # ⏳ Cron 애플리케이션 (예정)
└── bootstrap/                     # ⏳ 부트스트랩 (예정)
```

## 아키텍처 다이어그램

### 현재 구현된 Car & ParkingLot 도메인 아키텍처 (Phase 8 완료)

```
┌─────────────────────────────────────────────────────────┐
│                    외부 시스템                            │
│  ┌─────────────┐                    ┌─────────────┐      │
│  │   REST API  │                    │  Database   │      │
│  │  (Primary)  │                    │ (Secondary) │      │
│  │     ✅      │                    │     ✅      │      │
│  └─────────────┘                    └─────────────┘      │
│         │                                    ▲           │
│         ▼                                    │           │
│  ┌─────────────┐                    ┌─────────────┐      │
│  │Car/Parking  │                    │Car/Parking  │      │
│  │RestApi      │                    │JpaAdapter   │      │
│  │(Adapter)    │                    │(Adapter)    │      │
│  │     ✅      │                    │     ✅      │      │
│  └─────────────┘                    └─────────────┘      │
│         │                                    ▲           │
│         ▼                                    │           │
│  ┌─────────────┐                    ┌─────────────┐      │
│  │Car/Parking  │                    │Car/Parking  │      │
│  │Query/Command│                    │Load/Save    │      │
│  │UseCase      │                    │Port         │      │
│  │(Port-In)    │                    │(Port-Out)   │      │
│  │     ✅      │                    │     ✅      │      │
│  └─────────────┘                    └─────────────┘      │
│         │                                    ▲           │
│         ▼                                    │           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │          Car/ParkingLot Services                    │ │
│  │         (Application Services)                      │ │
│  │                    ✅                              │ │
│  └─────────────────────────────────────────────────────┘ │
│                           │                             │
│                           ▼                             │
│  ┌─────────────────────────────────────────────────────┐ │
│  │        Car & ParkingLot Domain Models               │ │
│  │  Car: LicensePlateNumber, CarEntity                 │ │
│  │  ParkingLot: ParkingLot, ParkingRecord, Status     │ │
│  │                    ✅                              │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                         │
│  ┌─────────────────────────────────────────────────────┐ │
│  │              JPA Infrastructure                     │ │
│  │  CarJpaEntity, CarJpaRepository, CarJpaAdapter     │ │
│  │                    ✅                              │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                         │
│  ┌─────────────────────────────────────────────────────┐ │
│  │              REST Infrastructure                    │ │
│  │  CarRequestDto, CarResponseDto, CarRestApi          │ │
│  │                    ✅                              │ │
│  └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 범례
- ✅ **구현 완료**: 테스트와 함께 완전히 구현됨
- 🚧 **다음 단계**: 곧 구현 예정
- ⏳ **계획됨**: 향후 구현 예정

## 구현 진행 상황

### Phase별 상세 진행률

| Phase | 상태 | 구현 내용 | 테스트 | 커밋 |
|-------|------|-----------|--------|------|
| Phase 0 | ✅ | 프로젝트 구조 설정 | ✅ | `0891944` |
| Phase 1 | ✅ | Car 도메인 구현 | ✅ | `63ced8f`, `4314fd5`, `f715ed7` |
| Phase 2 | ✅ | Car 포트 정의 | ✅ | `033378e`, `49cc08e` |
| Phase 3 | ✅ | Car 서비스 구현 | ✅ | `f64994e` |
| Phase 4 | ✅ | Car JPA 어댑터 | ✅ | `5215c19`, `9a0cd9c`, `4f44d61` |
| Phase 5 | ✅ | Car REST 어댑터 | ✅ | `56aab49`, `9428422` |
| Phase 6 | ✅ | ParkingLot 도메인 | ✅ | `026f92c`, `b7bcff3`, `8c2886b`, `a967838` |
| Phase 7 | ✅ | ParkingLot 어댑터 | ✅ | `0df7a4f`, `92a5423`, `590d0df` |
| Phase 8 | ✅ | 통합 애플리케이션 | ✅ | `c90deea` |
| Phase 9 | ⏳ | 최종 통합 테스트 | ⏳ | - |

### 전체 진행률: 89% (8/9 Phase 완료)

---

## 다음 단계

### Phase 9: 최종 통합 테스트 및 문서화
1. **전체 시스템 통합 테스트**: Car와 ParkingLot 도메인 통합 시나리오
2. **API 문서화**: Swagger/OpenAPI 문서 생성
3. **성능 테스트**: 부하 테스트 및 최적화

### 예상 완료 시점
- Phase 9: 2-3시간 예상
- 전체 프로젝트: 약 95% 완료 (현재 약 35-40시간 소요)

---

*이 문서는 각 Phase 완료 시마다 업데이트됩니다.*

**마지막 업데이트**: Phase 8 완료 (2025-06-28)
