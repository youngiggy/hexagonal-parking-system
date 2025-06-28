# 🚀 헥사고날 주차장 시스템 개선 로드맵

이 문서는 현재 프로젝트의 개선 사항과 향후 발전 방향을 제시합니다. 기여를 원하시는 분들은 이 로드맵을 참고하여 프로젝트를 더욱 발전시켜 주세요!

## 📋 목차

- [우선순위별 개선 사항](#우선순위별-개선-사항)
- [아키텍처 개선](#아키텍처-개선)
- [코드 품질 향상](#코드-품질-향상)
- [기능 확장](#기능-확장)
- [성능 최적화](#성능-최적화)
- [운영 환경 개선](#운영-환경-개선)
- [기여 가이드](#기여-가이드)

## 🎯 우선순위별 개선 사항

### 🔥 High Priority (즉시 개선 필요)

#### 1. 예외 처리 전략 개선
**현재 문제점:**
```kotlin
// 모든 예외를 동일하게 처리
catch (e: Exception) {
    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
}
```

**개선 방안:**
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(CarNotFoundException::class)
    fun handleCarNotFound(e: CarNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("CAR_NOT_FOUND", e.message))
    }
    
    @ExceptionHandler(ParkingLotFullException::class)
    fun handleParkingLotFull(e: ParkingLotFullException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse("PARKING_LOT_FULL", e.message))
    }
    
    @ExceptionHandler(ValidationException::class)
    fun handleValidation(e: ValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("VALIDATION_ERROR", e.message))
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: Instant = Instant.now()
)
```

**예상 작업 시간:** 4-6시간  
**난이도:** ⭐⭐⭐

#### 2. 통합 테스트 환경 수정
**현재 문제점:**
- Spring 컨텍스트 로딩 실패
- 테스트 실행 불가

**개선 방안:**
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IntegratedParkingApiTest {
    // 실제 비즈니스 시나리오 테스트 구현
}
```

**예상 작업 시간:** 6-8시간  
**난이도:** ⭐⭐⭐⭐

### 🔶 Medium Priority (단기 개선)

#### 3. 도메인 모델 불변성 강화
**개선 목표:**
```kotlin
// Before: 가변 객체
data class CarEntity(
    val licencePlateNumber: LicensePlateNumber,
    var model: String,
    var color: String
)

// After: 불변 객체 + 팩토리 메서드
data class CarEntity private constructor(
    val licencePlateNumber: LicensePlateNumber,
    val model: CarModel,
    val color: CarColor,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(
            licensePlate: LicensePlateNumber,
            model: CarModel,
            color: CarColor
        ): CarEntity {
            val now = Instant.now()
            return CarEntity(licensePlate, model, color, now, now)
        }
    }
    
    fun updateModel(newModel: CarModel): CarEntity = 
        copy(model = newModel, updatedAt = Instant.now())
}
```

**예상 작업 시간:** 8-10시간  
**난이도:** ⭐⭐⭐

#### 4. Value Object 확장
**개선 목표:**
```kotlin
// 새로운 Value Objects 추가
data class CarModel(val value: String) {
    init {
        require(value.isNotBlank() && value.length <= 50) { 
            "차량 모델은 1-50자 사이여야 합니다" 
        }
    }
}

data class CarColor(val value: String) {
    init {
        require(value in VALID_COLORS) { 
            "유효하지 않은 차량 색상입니다: $value" 
        }
    }
    
    companion object {
        private val VALID_COLORS = setOf(
            "흰색", "검은색", "은색", "빨간색", "파란색", "회색"
        )
    }
}

data class ParkingSpaceNumber(val value: Int) {
    init {
        require(value > 0) { "주차 공간 번호는 양수여야 합니다" }
    }
}
```

**예상 작업 시간:** 6-8시간  
**난이도:** ⭐⭐⭐

### 🔷 Low Priority (장기 개선)

#### 5. 이벤트 기반 아키텍처 도입
**개선 목표:**
```kotlin
// 도메인 이벤트 정의
sealed class DomainEvent {
    abstract val occurredAt: Instant
    abstract val aggregateId: String
}

data class CarParkedEvent(
    val car: CarEntity,
    val parkingLot: ParkingLotEntity,
    val parkingSpace: ParkingSpaceNumber,
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = car.licencePlateNumber.value
) : DomainEvent()

data class CarLeftEvent(
    val car: CarEntity,
    val parkingLot: ParkingLotEntity,
    val duration: Duration,
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = car.licencePlateNumber.value
) : DomainEvent()

// 이벤트 퍼블리셔
interface DomainEventPublisher {
    fun publish(event: DomainEvent)
}

// 이벤트 핸들러 예시
@EventHandler
class ParkingStatisticsHandler {
    fun handle(event: CarParkedEvent) {
        // 주차 통계 업데이트
    }
    
    fun handle(event: CarLeftEvent) {
        // 주차 시간 통계 업데이트
    }
}
```

**예상 작업 시간:** 12-16시간  
**난이도:** ⭐⭐⭐⭐⭐

## 🏗️ 아키텍처 개선

### 1. 모듈 구조 일관성 개선
**현재 상태:**
```
car/
├── application/
│   ├── domain/      ✅
│   ├── exception/   ✅
│   ├── port-in/     ✅
│   ├── port-out/    ✅
│   └── service/     ✅

parking-lot/
└── application/
    └── domain/      ✅ (나머지 누락)
```

**개선 목표:**
```
parking-lot/
├── application/
│   ├── domain/      ✅
│   ├── exception/   🆕
│   ├── port-in/     🆕
│   ├── port-out/    🆕
│   └── service/     🆕
├── adapter-jpa/     ✅ (이미 존재)
└── adapter-rest/    ✅ (이미 존재)
```

### 2. 도메인 서비스 인터페이스 추가
```kotlin
// 도메인 서비스 추상화
interface CarDomainService {
    fun registerCar(request: CarRegistrationRequest): CarEntity
    fun findCar(licensePlate: LicensePlateNumber): CarEntity?
    fun unregisterCar(licensePlate: LicensePlateNumber)
}

interface ParkingLotDomainService {
    fun createOrGetParkingLot(name: ParkingLotName, totalSpaces: ParkingSpaceCount): ParkingLotEntity
    fun parkCar(licensePlate: LicensePlateNumber, parkingLotName: ParkingLotName): ParkingRecordEntity
    fun leaveCar(licensePlate: LicensePlateNumber): ParkingRecordEntity
}

// 통합 서비스에서 인터페이스 의존
class IntegratedParkingService(
    private val carDomainService: CarDomainService,
    private val parkingLotDomainService: ParkingLotDomainService
)
```

## 💎 코드 품질 향상

### 1. 정적 분석 도구 도입
```kotlin
// build.gradle.kts에 추가
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

detekt {
    config = files("$projectDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}
```

### 2. 테스트 커버리지 개선
```kotlin
// JaCoCo 설정 강화
jacoco {
    toolVersion = "0.8.8"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec"))
    
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% 커버리지 요구
            }
        }
    }
}
```

### 3. 아키텍처 테스트 추가
```kotlin
// ArchUnit을 사용한 아키텍처 규칙 검증
@AnalyzeClasses(packages = ["com.example.hexagonal"])
class ArchitectureTest {
    
    @ArchTest
    val domainShouldNotDependOnInfrastructure = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adapter..", "..infrastructure..")
    
    @ArchTest
    val servicesShouldOnlyBeAccessedByAdapters = 
        classes()
            .that().resideInAPackage("..service..")
            .should().onlyBeAccessed()
            .byClassesThat().resideInAnyPackage("..adapter..", "..service..")
}
```

## 🚀 기능 확장

### 1. 주차 요금 계산 기능
```kotlin
// 새로운 도메인 모델
data class ParkingFee(
    val amount: BigDecimal,
    val currency: Currency = Currency.getInstance("KRW")
) {
    init {
        require(amount >= BigDecimal.ZERO) { "주차 요금은 음수일 수 없습니다" }
    }
}

data class ParkingRate(
    val baseRate: ParkingFee,        // 기본 요금
    val hourlyRate: ParkingFee,      // 시간당 요금
    val maxDailyRate: ParkingFee     // 일일 최대 요금
)

// 요금 계산 서비스
interface ParkingFeeCalculator {
    fun calculateFee(duration: Duration, rate: ParkingRate): ParkingFee
}
```

### 2. 주차장 예약 시스템
```kotlin
// 예약 도메인 모델
data class ParkingReservation(
    val id: ReservationId,
    val licensePlate: LicensePlateNumber,
    val parkingLotName: ParkingLotName,
    val reservedFrom: Instant,
    val reservedUntil: Instant,
    val status: ReservationStatus
)

enum class ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}

// 예약 서비스
interface ParkingReservationService {
    fun makeReservation(request: ReservationRequest): ParkingReservation
    fun cancelReservation(reservationId: ReservationId)
    fun confirmReservation(reservationId: ReservationId)
}
```

### 3. 실시간 주차 현황 조회
```kotlin
// 실시간 현황 DTO
data class ParkingLotStatus(
    val name: String,
    val totalSpaces: Int,
    val occupiedSpaces: Int,
    val availableSpaces: Int,
    val occupancyRate: Double,
    val lastUpdated: Instant
)

// WebSocket을 통한 실시간 업데이트
@Controller
class ParkingStatusWebSocketController {
    
    @MessageMapping("/parking-status")
    @SendTo("/topic/parking-status")
    fun getParkingStatus(): ParkingLotStatus {
        // 실시간 주차 현황 반환
    }
}
```

## ⚡ 성능 최적화

### 1. 캐싱 전략 도입
```kotlin
// Redis 캐싱 설정
@Configuration
@EnableCaching
class CacheConfig {
    
    @Bean
    fun cacheManager(): CacheManager {
        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration())
            .build()
    }
    
    private fun cacheConfiguration(): RedisCacheConfiguration {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
    }
}

// 서비스에서 캐싱 적용
@Service
class ParkingLotService {
    
    @Cacheable(value = ["parking-lot-status"], key = "#parkingLotName")
    fun getParkingLotStatus(parkingLotName: ParkingLotName): ParkingLotStatus {
        // 주차장 현황 조회
    }
    
    @CacheEvict(value = ["parking-lot-status"], key = "#parkingLotName")
    fun updateParkingStatus(parkingLotName: ParkingLotName) {
        // 주차 상태 변경 시 캐시 무효화
    }
}
```

### 2. 데이터베이스 최적화
```kotlin
// 인덱스 추가
@Entity
@Table(
    name = "parking_records",
    indexes = [
        Index(name = "idx_license_plate", columnList = "license_plate_number"),
        Index(name = "idx_parking_lot_status", columnList = "parking_lot_name, status"),
        Index(name = "idx_parked_at", columnList = "parked_at")
    ]
)
class ParkingRecordJpaEntity {
    // ...
}

// 배치 처리 최적화
@Repository
class CarJpaAdapter {
    
    @Modifying
    @Query("UPDATE CarJpaEntity c SET c.status = :status WHERE c.licensePlateNumber IN :licensePlates")
    fun updateCarStatusBatch(licensePlates: List<String>, status: String): Int
}
```

## 🔧 운영 환경 개선

### 1. 모니터링 및 로깅
```kotlin
// Micrometer 메트릭 추가
@Component
class ParkingMetrics(private val meterRegistry: MeterRegistry) {
    
    private val parkingCounter = Counter.builder("parking.events")
        .description("주차 이벤트 카운터")
        .register(meterRegistry)
    
    private val occupancyGauge = Gauge.builder("parking.occupancy.rate")
        .description("주차장 점유율")
        .register(meterRegistry) { getCurrentOccupancyRate() }
    
    fun recordParkingEvent(eventType: String) {
        parkingCounter.increment(Tags.of("type", eventType))
    }
}

// 구조화된 로깅
@Component
class ParkingLogger {
    
    private val logger = LoggerFactory.getLogger(ParkingLogger::class.java)
    
    fun logParkingEvent(event: String, licensePlate: String, parkingLot: String) {
        logger.info(
            "Parking event occurred",
            kv("event", event),
            kv("licensePlate", licensePlate),
            kv("parkingLot", parkingLot),
            kv("timestamp", Instant.now())
        )
    }
}
```

### 2. Docker 및 배포 환경
```dockerfile
# Dockerfile 개선
FROM openjdk:17-jdk-slim

# 보안 강화
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# 애플리케이션 복사
COPY build/libs/*.jar app.jar

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```yaml
# docker-compose.yml 추가
version: '3.8'
services:
  parking-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/parking
    depends_on:
      - postgres
      - redis
    
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: parking
      POSTGRES_USER: parking_user
      POSTGRES_PASSWORD: parking_pass
    volumes:
      - postgres_data:/var/lib/postgresql/data
    
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

## 📚 기여 가이드

### 🎯 기여하기 전에 확인할 사항

1. **이슈 확인**: GitHub Issues에서 관련 이슈가 있는지 확인
2. **논의**: 큰 변경사항은 먼저 Discussion에서 논의
3. **브랜치 전략**: `feature/개선사항-이름` 형태로 브랜치 생성

### 🔧 개발 환경 설정

```bash
# 1. 저장소 포크 및 클론
git clone https://github.com/your-username/hexagonal-parking-system.git
cd hexagonal-parking-system

# 2. 개발 브랜치 생성
git checkout -b feature/exception-handling-improvement

# 3. 개발 환경 확인
./gradlew build
./gradlew test

# 4. 개발 진행
# ... 코드 작성 ...

# 5. 테스트 실행
./gradlew test
./gradlew jacocoTestReport

# 6. 정적 분석 (추가 예정)
./gradlew ktlintCheck
./gradlew detekt
```

### 📝 커밋 메시지 규칙

```
타입(스코프): 간단한 설명

상세한 설명 (선택사항)

- 변경사항 1
- 변경사항 2

Closes #이슈번호
```

**타입:**
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `refactor`: 코드 리팩토링
- `test`: 테스트 추가/수정
- `docs`: 문서 수정
- `style`: 코드 스타일 변경
- `perf`: 성능 개선

---

## 🎯 마무리

이 로드맵은 프로젝트의 지속적인 발전을 위한 가이드입니다. 각 개선사항은 독립적으로 진행할 수 있도록 설계되었으며, 여러분의 기여를 기다리고 있습니다!

**질문이나 제안사항이 있으시면:**
- GitHub Issues에 등록
- GitHub Discussions에서 논의
- 이메일: dev@hexagonal-parking.com

함께 더 나은 헥사고날 아키텍처 예제를 만들어 나가요! 🚀
