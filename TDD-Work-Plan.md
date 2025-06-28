# TDD 작업 계획서
# 헥사고날 주차장 시스템 구현

## 1. 개요

### 1.1 목표
기존 헥사고날 주차장 시스템을 TDD(Test-Driven Development) 방법론을 사용하여 단계별로 재구현합니다.

### 1.2 TDD 원칙
- **Red**: 실패하는 테스트 먼저 작성
- **Green**: 테스트를 통과하는 최소한의 코드 작성
- **Refactor**: 코드 개선 및 리팩토링
- **반복**: 각 기능마다 Red-Green-Refactor 사이클 적용

### 1.3 구현 순서 원칙
1. **도메인 우선**: 외부 의존성 없는 순수한 도메인 로직부터 시작
2. **내부에서 외부로**: 도메인 → 애플리케이션 서비스 → 어댑터 순서
3. **단순에서 복잡으로**: 기본 기능부터 고급 기능까지 점진적 구현
4. **독립적 모듈**: 각 모듈을 독립적으로 개발 및 테스트

## 2. 프로젝트 구조 설정

### Phase 0: 프로젝트 초기 설정
**목표**: 멀티모듈 Gradle 프로젝트 구조 생성

#### 작업 내용:
1. **루트 프로젝트 설정**
   - `build.gradle.kts` 설정
   - `settings.gradle.kts` 설정
   - 공통 의존성 및 플러그인 설정

2. **모듈 구조 생성**
   ```
   car/
   ├── application/
   │   ├── domain/
   │   ├── exception/
   │   ├── port-in/
   │   ├── port-out/
   │   └── service/
   ├── adapter-jpa/
   └── adapter-rest/
   
   parking-lot/
   ├── application/
   │   ├── domain/
   │   ├── port-in/
   │   └── service/
   ├── adapter-cron/
   └── adapter-rest/
   
   application-api/
   application-cron/
   bootstrap/
   ```

3. **테스트 환경 설정**
   - Kotest 설정
   - MockK 설정
   - 테스트 유틸리티 클래스

#### 완료 기준:
- [ ] 모든 모듈이 빌드 성공
- [ ] 기본 테스트가 실행됨
- [ ] IDE에서 프로젝트 구조 인식

---

## 3. Phase 1: Car 도메인 구현

### 3.1 Step 1-1: LicensePlateNumber 값 객체 구현

**TDD 사이클:**

#### Red (실패하는 테스트 작성)
```kotlin
// car/application/domain/src/test/kotlin/.../LicensePlateNumberTest.kt
class LicensePlateNumberTest : StringSpec({
    "유효한 번호판 형식이면 생성된다" {
        // given
        val validPlateNumber = "서울 123 가 1234"
        
        // when & then
        shouldNotThrow<IllegalArgumentException> {
            LicensePlateNumber(validPlateNumber)
        }
    }
    
    "잘못된 번호판 형식이면 예외가 발생한다" {
        // given
        val invalidPlateNumber = "invalid"
        
        // when & then
        shouldThrow<IllegalArgumentException> {
            LicensePlateNumber(invalidPlateNumber)
        }
    }
})
```

#### Green (테스트 통과하는 최소 코드)
```kotlin
// car/application/domain/src/main/kotlin/.../LicensePlateNumber.kt
@JvmInline
value class LicensePlateNumber(val value: String) {
    init {
        require(SPEC_LICENCE_PLATE_NUMBER.matches(value)) {
            "자동차 번호 형식이 유효하지 않습니다"
        }
    }
    
    companion object {
        val SPEC_LICENCE_PLATE_NUMBER = "[가-힣]{0,2}\\s?[0-9]{1,3}\\s?[가-힣]\\s?[0-9]{4}".toRegex()
    }
}
```

#### Refactor
- 정규식 패턴 최적화
- 에러 메시지 개선
- 추가 테스트 케이스 작성

### 3.2 Step 1-2: Car 도메인 모델 구현

**TDD 사이클:**

#### Red
```kotlin
class CarTest : StringSpec({
    "Car 엔티티가 생성된다" {
        // given
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val carData = CarData(licensePlateNumber)
        val carKey = CarKey(UUID.randomUUID())
        val now = Instant.now()
        
        // when
        val car = CarEntity(
            licensePlateNumber = licensePlateNumber,
            identity = carKey,
            createdAt = now,
            updatedAt = now
        )
        
        // then
        car.licensePlateNumber shouldBe licensePlateNumber
        car.identity shouldBe carKey
        car.createdAt shouldBe now
        car.updatedAt shouldBe now
    }
})
```

#### Green
```kotlin
// Car 인터페이스들과 데이터 클래스 구현
interface CarProperties {
    val licencePlateNumber: LicensePlateNumber
}

interface CarIdentity {
    val value: UUID
}

interface CarModel : CarProperties {
    val identity: CarIdentity
    val createdAt: Instant
    val updatedAt: Instant
}

data class CarData(
    override val licencePlateNumber: LicensePlateNumber,
) : CarProperties

data class CarKey(
    override val value: UUID,
) : CarIdentity

data class CarEntity(
    override val licencePlateNumber: LicensePlateNumber,
    override val identity: CarIdentity,
    override val createdAt: Instant,
    override val updatedAt: Instant,
) : CarModel
```

### 3.3 Step 1-3: Car 예외 클래스 구현

**TDD 사이클:**

#### Red
```kotlin
class CarNotFoundExceptionTest : StringSpec({
    "CarNotFoundException이 생성된다" {
        // given
        val message = "등록되지 않은 자동차입니다"
        
        // when
        val exception = CarNotFoundException(message)
        
        // then
        exception.message shouldBe message
        exception shouldBe instanceOf<RuntimeException>()
    }
})
```

#### Green
```kotlin
class CarNotFoundException(message: String) : RuntimeException(message)
```

#### 완료 기준:
- [ ] 모든 도메인 모델 테스트 통과
- [ ] 번호판 검증 로직 정상 동작
- [ ] 예외 처리 테스트 통과

---

## 4. Phase 2: Car 포트 정의

### 4.1 Step 2-1: Car 인바운드 포트 구현

**TDD 사이클:**

#### Red
```kotlin
class CarQueryUseCaseTest : StringSpec({
    "번호판으로 차량을 조회한다" {
        // given
        val mockUseCase = mockk<CarQueryUseCase>()
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val expectedCar = mockk<CarModel>()
        
        every { mockUseCase.getByLicensePlateNumber(licensePlateNumber) } returns expectedCar
        
        // when
        val result = mockUseCase.getByLicensePlateNumber(licensePlateNumber)
        
        // then
        result shouldBe expectedCar
    }
})
```

#### Green
```kotlin
interface CarQueryUseCase {
    /**
     * @throws CarNotFoundException
     */
    fun getByLicensePlateNumber(licensePlateNumber: LicensePlateNumber): CarModel
}

interface CarCommandUseCase {
    fun bulkCreateCar(commands: Collection<CarProperties>): Collection<CarModel>
}
```

### 4.2 Step 2-2: Car 아웃바운드 포트 구현

#### Red
```kotlin
class CarLoadPortTest : StringSpec({
    "번호판으로 차량을 조회한다" {
        // given
        val mockPort = mockk<CarLoadPort>()
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val expectedCar = mockk<CarModel>()
        
        every { mockPort.findByLicensePlateNumberOrNull(licensePlateNumber) } returns expectedCar
        
        // when
        val result = mockPort.findByLicensePlateNumberOrNull(licensePlateNumber)
        
        // then
        result shouldBe expectedCar
    }
})
```

#### Green
```kotlin
interface CarLoadPort {
    fun findByLicensePlateNumberOrNull(licensePlateNumber: LicensePlateNumber): CarModel?
}

interface CarSavePort {
    fun saveAll(cars: Collection<CarProperties>): Collection<CarModel>
}
```

#### 완료 기준:
- [ ] 모든 포트 인터페이스 정의 완료
- [ ] 포트 테스트 통과
- [ ] 인터페이스 문서화 완료

---

## 5. Phase 3: Car 서비스 구현

### 5.1 Step 3-1: CarService 구현

**TDD 사이클:**

#### Red
```kotlin
class CarServiceTest : StringSpec({
    "차량을 일괄 등록한다" {
        // given
        val mockSavePort = mockk<CarSavePort>()
        val mockLoadPort = mockk<CarLoadPort>()
        val service = CarService(mockSavePort, mockLoadPort)
        
        val carData = listOf(CarData(LicensePlateNumber("서울 123 가 1234")))
        val expectedCars = listOf(mockk<CarModel>())
        
        every { mockSavePort.saveAll(carData) } returns expectedCars
        
        // when
        val result = service.bulkCreateCar(carData)
        
        // then
        result shouldBe expectedCars
        verify { mockSavePort.saveAll(carData) }
    }
    
    "번호판으로 차량을 조회한다" {
        // given
        val mockSavePort = mockk<CarSavePort>()
        val mockLoadPort = mockk<CarLoadPort>()
        val service = CarService(mockSavePort, mockLoadPort)
        
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val expectedCar = mockk<CarModel>()
        
        every { mockLoadPort.findByLicensePlateNumberOrNull(licensePlateNumber) } returns expectedCar
        
        // when
        val result = service.getByLicensePlateNumber(licensePlateNumber)
        
        // then
        result shouldBe expectedCar
    }
    
    "등록되지 않은 차량 조회시 예외가 발생한다" {
        // given
        val mockSavePort = mockk<CarSavePort>()
        val mockLoadPort = mockk<CarLoadPort>()
        val service = CarService(mockSavePort, mockLoadPort)
        
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        
        every { mockLoadPort.findByLicensePlateNumberOrNull(licensePlateNumber) } returns null
        
        // when & then
        shouldThrow<CarNotFoundException> {
            service.getByLicensePlateNumber(licensePlateNumber)
        }
    }
})
```

#### Green
```kotlin
@Transactional
open class CarService(
    private val savePort: CarSavePort,
    private val loadPort: CarLoadPort,
) : CarCommandUseCase, CarQueryUseCase {
    
    override fun bulkCreateCar(commands: Collection<CarProperties>): Collection<CarModel> {
        return savePort.saveAll(commands)
    }
    
    override fun getByLicensePlateNumber(licensePlateNumber: LicensePlateNumber): CarModel {
        return loadPort.findByLicensePlateNumberOrNull(licensePlateNumber)
            ?: throw CarNotFoundException("등록되지 않은 자동차입니다")
    }
}
```

#### 완료 기준:
- [ ] 모든 서비스 테스트 통과
- [ ] 트랜잭션 처리 확인
- [ ] 예외 처리 로직 검증

---

## 6. Phase 4: Car JPA 어댑터 구현

### 6.1 Step 4-1: CarJpaEntity 구현

**TDD 사이클:**

#### Red
```kotlin
class CarJpaEntityTest : StringSpec({
    "CarJpaEntity가 생성된다" {
        // given
        val licensePlateNumber = "서울 123 가 1234"
        val carData = CarData(LicensePlateNumber(licensePlateNumber))
        
        // when
        val entity = CarJpaEntity(carData)
        
        // then
        entity.licensePlateNumber shouldBe licensePlateNumber
        entity.id shouldNotBe null
        entity.createdAt shouldNotBe null
        entity.updatedAt shouldNotBe null
    }
    
    "CarJpaEntity를 CarModel로 변환한다" {
        // given
        val entity = CarJpaEntity().apply {
            licensePlateNumber = "서울 123 가 1234"
        }
        
        // when
        val model = entity.toModel()
        
        // then
        model.licensePlateNumber.value shouldBe "서울 123 가 1234"
        model.identity.value shouldBe entity.id
    }
})
```

#### Green
```kotlin
@Entity
@Table(name = "cars")
class CarJpaEntity(
    @Column(name = "license_plate_number", nullable = false, unique = true)
    var licensePlateNumber: String = "",
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID = UUID.randomUUID(),
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    constructor(carProperties: CarProperties) : this(
        licensePlateNumber = carProperties.licencePlateNumber.value
    )
    
    fun toModel(): CarModel = CarEntity(
        licensePlateNumber = LicensePlateNumber(licensePlateNumber),
        identity = CarKey(id),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
```

### 6.2 Step 4-2: CarJpaRepository 구현

#### Red
```kotlin
class CarJpaRepositoryTest : StringSpec({
    "번호판으로 차량을 조회한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val licensePlateNumber = "서울 123 가 1234"
        val expectedEntity = mockk<CarJpaEntity>()
        
        every { mockRepository.findByLicensePlateNumber(licensePlateNumber) } returns expectedEntity
        
        // when
        val result = mockRepository.findByLicensePlateNumber(licensePlateNumber)
        
        // then
        result shouldBe expectedEntity
    }
})
```

#### Green
```kotlin
interface CarJpaRepository : JpaRepository<CarJpaEntity, UUID> {
    fun findByLicensePlateNumber(licensePlateNumber: String): CarJpaEntity?
}
```

### 6.3 Step 4-3: CarJpaAdapter 구현

#### Red
```kotlin
class CarJpaAdapterTest : StringSpec({
    "차량을 저장한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val adapter = CarJpaAdapter(mockRepository)
        
        val carData = listOf(CarData(LicensePlateNumber("서울 123 가 1234")))
        val savedEntity = mockk<CarJpaEntity>()
        val expectedModel = mockk<CarModel>()
        
        every { mockRepository.saveAll(any<List<CarJpaEntity>>()) } returns listOf(savedEntity)
        every { savedEntity.toModel() } returns expectedModel
        
        // when
        val result = adapter.saveAll(carData)
        
        // then
        result shouldHaveSize 1
        result.first() shouldBe expectedModel
    }
    
    "번호판으로 차량을 조회한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val adapter = CarJpaAdapter(mockRepository)
        
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val foundEntity = mockk<CarJpaEntity>()
        val expectedModel = mockk<CarModel>()
        
        every { mockRepository.findByLicensePlateNumber(licensePlateNumber.value) } returns foundEntity
        every { foundEntity.toModel() } returns expectedModel
        
        // when
        val result = adapter.findByLicensePlateNumberOrNull(licensePlateNumber)
        
        // then
        result shouldBe expectedModel
    }
})
```

#### Green
```kotlin
class CarJpaAdapter(
    private val repository: CarJpaRepository,
) : CarSavePort, CarLoadPort {
    
    override fun saveAll(cars: Collection<CarProperties>): Collection<CarModel> {
        return cars
            .map { CarJpaEntity(it) }
            .let { repository.saveAll(it) }
            .map { it.toModel() }
    }
    
    override fun findByLicensePlateNumberOrNull(licensePlateNumber: LicensePlateNumber): CarModel? {
        return repository
            .findByLicensePlateNumber(licensePlateNumber.value)
            ?.toModel()
    }
}
```

#### 완료 기준:
- [ ] JPA 엔티티 테스트 통과
- [ ] 리포지토리 인터페이스 정의 완료
- [ ] 어댑터 구현 및 테스트 통과

---

## 7. Phase 5: Car REST 어댑터 구현

### 7.1 Step 5-1: CarDto 구현

**TDD 사이클:**

#### Red
```kotlin
class CarDtoTest : StringSpec({
    "CarModel을 CarDto로 변환한다" {
        // given
        val carModel = mockk<CarModel> {
            every { identity.value } returns UUID.randomUUID()
            every { licencePlateNumber.value } returns "서울 123 가 1234"
            every { createdAt } returns Instant.now()
            every { updatedAt } returns Instant.now()
        }
        
        // when
        val dto = CarDto(carModel)
        
        // then
        dto.id shouldBe carModel.identity.value
        dto.licensePlateNumber shouldBe carModel.licencePlateNumber.value
    }
    
    "CarDto를 CarProperties로 변환한다" {
        // given
        val dto = CarDto(
            licensePlateNumber = "서울 123 가 1234"
        )
        
        // when
        val props = dto.toProps()
        
        // then
        props.licencePlateNumber.value shouldBe "서울 123 가 1234"
    }
})
```

#### Green
```kotlin
data class CarDto(
    val id: UUID? = null,
    @field:NotBlank
    val licensePlateNumber: String,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
) {
    constructor(model: CarModel) : this(
        id = model.identity.value,
        licensePlateNumber = model.licencePlateNumber.value,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt
    )
    
    fun toProps(): CarProperties = CarData(
        licencePlateNumber = LicensePlateNumber(licensePlateNumber)
    )
}
```

### 7.2 Step 5-2: CarRestApi 구현

#### Red
```kotlin
@WebMvcTest(CarRestApi::class)
class CarRestApiTest : StringSpec({
    "차량을 등록한다" {
        // given
        val mockCommandUseCase = mockk<CarCommandUseCase>()
        val mockQueryUseCase = mockk<CarQueryUseCase>()
        
        val requestDto = listOf(CarDto(licensePlateNumber = "서울 123 가 1234"))
        val expectedModel = mockk<CarModel>()
        val expectedDto = CarDto(expectedModel)
        
        every { mockCommandUseCase.bulkCreateCar(any()) } returns listOf(expectedModel)
        
        // when & then
        mockMvc.perform(
            post("/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].licensePlateNumber").value("서울 123 가 1234"))
    }
    
    "차량을 조회한다" {
        // given
        val licensePlateNumber = "서울 123 가 1234"
        val expectedModel = mockk<CarModel>()
        
        every { mockQueryUseCase.getByLicensePlateNumber(any()) } returns expectedModel
        
        // when & then
        mockMvc.perform(
            get("/cars")
                .param("licensePlateNumber", licensePlateNumber)
        )
        .andExpect(status().isOk)
    }
})
```

#### Green
```kotlin
@RestController
@RequestMapping(path = ["/cars"])
@Validated
class CarRestApi(
    private val commandUseCase: CarCommandUseCase,
    private val queryUseCase: CarQueryUseCase,
) {
    @Operation(tags = ["Cars"], summary = "차량 등록")
    @PostMapping
    fun bulkCreateCars(
        @Valid @RequestBody request: Collection<CarDto>,
    ): Collection<CarDto> {
        return commandUseCase
            .bulkCreateCar(
                commands = request.map { it.toProps() },
            )
            .map { CarDto(it) }
    }
    
    @Operation(tags = ["Cars"], summary = "차량 조회")
    @GetMapping
    fun getCar(
        @RequestParam licensePlateNumber: String,
    ): CarDto {
        return queryUseCase
            .getByLicensePlateNumber(LicensePlateNumber(licensePlateNumber))
            .let { CarDto(it) }
    }
}
```

#### 완료 기준:
- [ ] DTO 변환 로직 테스트 통과
- [ ] REST API 테스트 통과
- [ ] 입력 검증 테스트 통과

---
## 8. Phase 6: ParkingLot 도메인 구현

### 8.1 Step 6-1: Money 값 객체 구현

**TDD 사이클:**

#### Red
```kotlin
class MoneyTest : StringSpec({
    "Money 객체가 생성된다" {
        // given
        val amount = BigDecimal("1000")
        
        // when
        val money = Money(amount)
        
        // then
        money.amount shouldBe amount
    }
    
    "Money 객체끼리 더할 수 있다" {
        // given
        val money1 = Money(BigDecimal("1000"))
        val money2 = Money(BigDecimal("500"))
        
        // when
        val result = money1 + money2
        
        // then
        result.amount shouldBe BigDecimal("1500")
    }
})
```

#### Green
```kotlin
@JvmInline
value class Money(val amount: BigDecimal) {
    operator fun plus(other: Money): Money = Money(amount + other.amount)
    operator fun times(multiplier: Long): Money = Money(amount * BigDecimal(multiplier))
    
    companion object {
        val ZERO = Money(BigDecimal.ZERO)
    }
}
```

### 8.2 Step 6-2: ParkingFeePolicy 구현

#### Red
```kotlin
class ParkingFeePolicyTest : StringSpec({
    "기본 요금 정책으로 요금을 계산한다" {
        // given
        val policy = BasicParkingFeePolicy(
            baseAmount = Money(BigDecimal("1000")),
            baseDuration = Duration.ofHours(1)
        )
        val duration = Duration.ofHours(2)
        
        // when
        val fee = policy.calculate(duration)
        
        // then
        fee shouldBe Money(BigDecimal("2000"))
    }
})
```

#### Green
```kotlin
interface ParkingFeePolicy {
    fun calculate(duration: Duration): Money
}

data class BasicParkingFeePolicy(
    val baseAmount: Money,
    val baseDuration: Duration
) : ParkingFeePolicy {
    override fun calculate(duration: Duration): Money {
        val hours = duration.toHours()
        return baseAmount * maxOf(1L, hours)
    }
}
```

### 8.3 Step 6-3: ParkingEvent 및 ParkingLot 구현

#### Red
```kotlin
class ParkingLotTest : StringSpec({
    "차량이 입차한다" {
        // given
        val parkingLot = ParkingLot()
        val car = mockk<CarModel>()
        val feePolicy = mockk<ParkingFeePolicy>()
        val enteredAt = Instant.now()
        
        // when
        val event = parkingLot.enter(car, feePolicy, enteredAt)
        
        // then
        event.car shouldBe car
        parkingLot.parkingEvents shouldContain event
    }
    
    "차량이 출차한다" {
        // given
        val parkingLot = ParkingLot()
        val car = mockk<CarModel>()
        val feePolicy = mockk<ParkingFeePolicy>()
        
        parkingLot.enter(car, feePolicy, Instant.now())
        
        // when
        val event = parkingLot.leave(car, Instant.now())
        
        // then
        event.leavedAt shouldNotBe null
    }
})
```

#### Green
```kotlin
data class ParkingEvent(
    val car: CarModel,
    val feePolicy: ParkingFeePolicy,
    val enteredAt: Instant,
    val leavedAt: Instant? = null,
) {
    val parkingDuration: Duration =
        if (leavedAt == null) {
            Duration.between(enteredAt, Instant.now())
        } else {
            Duration.between(enteredAt, leavedAt)
        }
    
    val parkingFee: Money = feePolicy.calculate(parkingDuration)
}

class ParkingLot(
    val parkingEvents: MutableSet<ParkingEvent> = Collections.synchronizedSet(mutableSetOf()),
) {
    fun enter(car: CarModel, feePolicy: ParkingFeePolicy, enteredAt: Instant): ParkingEvent {
        val event = ParkingEvent(car, feePolicy, enteredAt)
        parkingEvents.add(event)
        return event
    }
    
    fun leave(car: CarModel, leavedAt: Instant): ParkingEvent {
        val event = parkingEvents.firstOrNull { it.car == car }
            ?: throw CarNotFoundException("입차 기록이 없는 자동차입니다")
        
        parkingEvents.remove(event)
        return event.copy(leavedAt = leavedAt)
    }
}
```

---

## 9. Phase 7: ParkingLot 서비스 및 어댑터 구현

### 9.1 Step 7-1: ParkingLot 포트 정의

```kotlin
interface ParkingLotCommandUseCase {
    fun enterCar(car: CarModel, enteredAt: Instant): ParkingEvent
    fun leaveCar(car: CarModel, leavedAt: Instant): ParkingEvent
}

interface ParkingLotQueryUseCase {
    fun getCurrentParkingEvents(): Collection<ParkingEvent>
}
```

### 9.2 Step 7-2: ParkingLotService 구현

```kotlin
@Service
class ParkingLotService(
    private val parkingLot: ParkingLot,
    private val feePolicy: ParkingFeePolicy,
) : ParkingLotCommandUseCase, ParkingLotQueryUseCase {
    
    override fun enterCar(car: CarModel, enteredAt: Instant): ParkingEvent {
        return parkingLot.enter(car, feePolicy, enteredAt)
    }
    
    override fun leaveCar(car: CarModel, leavedAt: Instant): ParkingEvent {
        return parkingLot.leave(car, leavedAt)
    }
    
    override fun getCurrentParkingEvents(): Collection<ParkingEvent> {
        return parkingLot.parkingEvents.toList()
    }
}
```

---

## 10. Phase 8: 애플리케이션 통합

### 10.1 Step 8-1: Configuration 클래스 구현

```kotlin
@Configuration
@EnableAutoConfiguration
class CarServiceAutoConfiguration {
    @Bean
    fun carService(savePort: CarSavePort, loadPort: CarLoadPort): CarService {
        return CarService(savePort, loadPort)
    }
}

@Configuration
class ParkingLotConfiguration {
    @Bean
    fun parkingLot(): ParkingLot = ParkingLot()
    
    @Bean
    fun parkingFeePolicy(): ParkingFeePolicy = BasicParkingFeePolicy(
        baseAmount = Money(BigDecimal("1000")),
        baseDuration = Duration.ofHours(1)
    )
}
```

### 10.2 Step 8-2: 애플리케이션 부트스트랩

```kotlin
@SpringBootApplication
class HexagonalExampleApiApplication {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}

fun main(args: Array<String>) {
    runApplication<HexagonalExampleApiApplication>(*args)
}
```

---

## 11. Phase 9: 통합 테스트

### 11.1 Step 9-1: API 통합 테스트

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarApiIntegrationTest : StringSpec({
    "차량 등록 및 조회 통합 테스트" {
        // given
        val carDto = CarDto(licensePlateNumber = "서울 123 가 1234")
        
        // when - 차량 등록
        val createdCars = restTemplate.postForObject(
            "/cars",
            listOf(carDto),
            Array<CarDto>::class.java
        )
        
        // then
        createdCars shouldHaveSize 1
        
        // when - 차량 조회
        val foundCar = restTemplate.getForObject(
            "/cars?licensePlateNumber=서울 123 가 1234",
            CarDto::class.java
        )
        
        // then
        foundCar?.licensePlateNumber shouldBe "서울 123 가 1234"
    }
})
```

---

## 12. 완료 체크리스트

### Phase별 완료 기준

#### Phase 0: 프로젝트 설정
- [ ] 멀티모듈 Gradle 프로젝트 구조 생성
- [ ] 모든 모듈 빌드 성공
- [ ] 테스트 환경 설정 완료

#### Phase 1: Car 도메인
- [ ] LicensePlateNumber 값 객체 구현 및 테스트
- [ ] Car 도메인 모델 구현 및 테스트
- [ ] CarNotFoundException 구현 및 테스트

#### Phase 2: Car 포트
- [ ] CarQueryUseCase, CarCommandUseCase 정의
- [ ] CarLoadPort, CarSavePort 정의
- [ ] 포트 인터페이스 테스트

#### Phase 3: Car 서비스
- [ ] CarService 구현 및 단위 테스트
- [ ] 트랜잭션 처리 확인
- [ ] 예외 처리 로직 검증

#### Phase 4: Car JPA 어댑터
- [ ] CarJpaEntity 구현 및 테스트
- [ ] CarJpaRepository 정의
- [ ] CarJpaAdapter 구현 및 테스트

#### Phase 5: Car REST 어댑터
- [ ] CarDto 구현 및 변환 로직 테스트
- [ ] CarRestApi 구현 및 컨트롤러 테스트
- [ ] 입력 검증 테스트

#### Phase 6: ParkingLot 도메인
- [ ] Money 값 객체 구현 및 테스트
- [ ] ParkingFeePolicy 구현 및 테스트
- [ ] ParkingEvent, ParkingLot 구현 및 테스트

#### Phase 7: ParkingLot 서비스
- [ ] ParkingLot 포트 정의
- [ ] ParkingLotService 구현 및 테스트

#### Phase 8: 애플리케이션 통합
- [ ] Configuration 클래스 구현
- [ ] 애플리케이션 부트스트랩 구현
- [ ] 의존성 주입 확인

#### Phase 9: 통합 테스트
- [ ] API 통합 테스트 작성 및 실행
- [ ] 전체 시나리오 테스트
- [ ] 성능 및 동시성 테스트

### 최종 검증
- [ ] 모든 단위 테스트 통과 (커버리지 80% 이상)
- [ ] 모든 통합 테스트 통과
- [ ] API 문서화 완료 (Swagger)
- [ ] 헥사고날 아키텍처 원칙 준수 확인
- [ ] TDD 사이클 완전 적용 확인

---

## 13. 예상 소요 시간

| Phase | 예상 시간 | 주요 작업 |
|-------|----------|----------|
| Phase 0 | 2-3시간 | 프로젝트 구조 설정 |
| Phase 1 | 4-5시간 | Car 도메인 구현 |
| Phase 2 | 2-3시간 | Car 포트 정의 |
| Phase 3 | 3-4시간 | Car 서비스 구현 |
| Phase 4 | 4-5시간 | Car JPA 어댑터 |
| Phase 5 | 3-4시간 | Car REST 어댑터 |
| Phase 6 | 5-6시간 | ParkingLot 도메인 |
| Phase 7 | 3-4시간 | ParkingLot 서비스 |
| Phase 8 | 2-3시간 | 애플리케이션 통합 |
| Phase 9 | 4-5시간 | 통합 테스트 |
| **총합** | **32-42시간** | **전체 프로젝트** |

이 계획서를 따라 단계별로 TDD를 적용하여 헥사고날 주차장 시스템을 구현할 수 있습니다.
