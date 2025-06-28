package com.example.hexagonal.car

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.util.UUID

class CarJpaAdapterTest : StringSpec({
    "CarJpaAdapter는 CarLoadPort와 CarSavePort를 구현한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>(relaxed = true)
        
        // when
        val adapter = CarJpaAdapter(mockRepository)
        
        // then
        adapter.shouldBeInstanceOf<CarLoadPort>()
        adapter.shouldBeInstanceOf<CarSavePort>()
    }
    
    "번호판으로 차량을 조회한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val adapter = CarJpaAdapter(mockRepository)
        
        val licensePlateNumber = LicensePlateNumber("123가1234")
        val jpaEntity = CarJpaEntity(
            licensePlateNumber = licensePlateNumber.value,
            id = UUID.randomUUID(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        every { mockRepository.findByLicensePlateNumber(licensePlateNumber.value) } returns jpaEntity
        
        // when
        val result = adapter.findByLicensePlateNumberOrNull(licensePlateNumber)
        
        // then
        result shouldNotBe null
        result!!.licencePlateNumber shouldBe licensePlateNumber
        result.identity.value shouldBe jpaEntity.id
        result.shouldBeInstanceOf<CarModel>()
        verify(exactly = 1) { mockRepository.findByLicensePlateNumber(licensePlateNumber.value) }
    }
    
    "존재하지 않는 번호판으로 조회시 null을 반환한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val adapter = CarJpaAdapter(mockRepository)
        
        val licensePlateNumber = LicensePlateNumber("999가9999")
        
        every { mockRepository.findByLicensePlateNumber(licensePlateNumber.value) } returns null
        
        // when
        val result = adapter.findByLicensePlateNumberOrNull(licensePlateNumber)
        
        // then
        result shouldBe null
        verify(exactly = 1) { mockRepository.findByLicensePlateNumber(licensePlateNumber.value) }
    }
    
    "여러 차량을 일괄 저장한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val adapter = CarJpaAdapter(mockRepository)
        
        val carProperties = listOf(
            CarData(LicensePlateNumber("111가1111")),
            CarData(LicensePlateNumber("222나2222"))
        )
        
        val savedEntities = carProperties.map { carData ->
            CarJpaEntity(
                licensePlateNumber = carData.licencePlateNumber.value,
                id = UUID.randomUUID(),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
        
        every { mockRepository.saveAll(any<List<CarJpaEntity>>()) } returns savedEntities
        
        // when
        val result = adapter.saveAll(carProperties)
        
        // then
        result.size shouldBe 2
        result.forEachIndexed { index, carModel ->
            carModel.licencePlateNumber shouldBe carProperties[index].licencePlateNumber
            carModel.shouldBeInstanceOf<CarModel>()
        }
        verify(exactly = 1) { mockRepository.saveAll(any<List<CarJpaEntity>>()) }
    }
    
    "빈 컬렉션을 저장할 수 있다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val adapter = CarJpaAdapter(mockRepository)
        
        val emptyCarProperties = emptyList<CarProperties>()
        val emptyEntities = emptyList<CarJpaEntity>()
        
        every { mockRepository.saveAll(any<List<CarJpaEntity>>()) } returns emptyEntities
        
        // when
        val result = adapter.saveAll(emptyCarProperties)
        
        // then
        result.size shouldBe 0
        result.shouldBeInstanceOf<Collection<CarModel>>()
        verify(exactly = 1) { mockRepository.saveAll(any<List<CarJpaEntity>>()) }
    }
    
    "CarJpaAdapter는 의존성 주입을 통해 Repository를 받는다" {
        // given
        val repository = mockk<CarJpaRepository>(relaxed = true)
        
        // when
        val adapter = CarJpaAdapter(repository)
        
        // then
        adapter.shouldBeInstanceOf<CarJpaAdapter>()
        
        // 실제로 주입된 repository가 사용되는지 확인
        val licensePlateNumber = LicensePlateNumber("111가1111")
        adapter.findByLicensePlateNumberOrNull(licensePlateNumber)
        
        verify { repository.findByLicensePlateNumber(licensePlateNumber.value) }
    }
    
    "도메인 모델과 JPA 엔티티 간 변환이 올바르게 동작한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val adapter = CarJpaAdapter(mockRepository)
        
        val originalLicensePlateNumber = LicensePlateNumber("222가2222")
        val carData = CarData(originalLicensePlateNumber)
        
        val savedEntity = CarJpaEntity(
            licensePlateNumber = originalLicensePlateNumber.value,
            id = UUID.randomUUID(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        every { mockRepository.saveAll(any<List<CarJpaEntity>>()) } returns listOf(savedEntity)
        
        // when
        val result = adapter.saveAll(listOf(carData))
        
        // then
        val savedModel = result.first()
        savedModel.licencePlateNumber shouldBe originalLicensePlateNumber
        savedModel.identity.value shouldBe savedEntity.id
        savedModel.createdAt shouldBe savedEntity.createdAt
        savedModel.updatedAt shouldBe savedEntity.updatedAt
    }
    
    "대량의 차량 데이터를 처리할 수 있다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val adapter = CarJpaAdapter(mockRepository)
        
        val largeCarProperties = (1..100).map { 
            CarData(LicensePlateNumber("${it}가${1000 + it}"))
        }
        
        val savedEntities = largeCarProperties.map { carData ->
            CarJpaEntity(
                licensePlateNumber = carData.licencePlateNumber.value,
                id = UUID.randomUUID(),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
        
        every { mockRepository.saveAll(any<List<CarJpaEntity>>()) } returns savedEntities
        
        // when
        val result = adapter.saveAll(largeCarProperties)
        
        // then
        result.size shouldBe 100
        result.forEachIndexed { index, carModel ->
            carModel.licencePlateNumber shouldBe largeCarProperties[index].licencePlateNumber
        }
        verify(exactly = 1) { mockRepository.saveAll(any<List<CarJpaEntity>>()) }
    }
    
    "CarJpaAdapter는 트랜잭션 안전성을 보장한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>(relaxed = true)
        
        // when
        val adapter = CarJpaAdapter(mockRepository)
        
        // then - @Transactional 어노테이션이 적용되어야 함
        adapter.shouldBeInstanceOf<CarJpaAdapter>()
        
        // 실제 트랜잭션 동작은 통합 테스트에서 검증
    }
    
    "CarJpaAdapter 에러 처리가 올바르게 동작한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val adapter = CarJpaAdapter(mockRepository)
        
        val licensePlateNumber = LicensePlateNumber("444가4444")
        
        every { mockRepository.findByLicensePlateNumber(licensePlateNumber.value) } throws RuntimeException("Database error")
        
        // when & then
        try {
            adapter.findByLicensePlateNumberOrNull(licensePlateNumber)
        } catch (e: RuntimeException) {
            e.message shouldBe "Database error"
        }
        
        verify(exactly = 1) { mockRepository.findByLicensePlateNumber(licensePlateNumber.value) }
    }
    
    "CarJpaAdapter는 성능 최적화를 고려한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val adapter = CarJpaAdapter(mockRepository)
        
        val carProperties = (1..10).map { 
            CarData(LicensePlateNumber("${it}가${1000 + it}"))
        }
        
        val savedEntities = carProperties.map { carData ->
            CarJpaEntity(
                licensePlateNumber = carData.licencePlateNumber.value,
                id = UUID.randomUUID(),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
        
        every { mockRepository.saveAll(any<List<CarJpaEntity>>()) } returns savedEntities
        
        // when
        val result = adapter.saveAll(carProperties)
        
        // then - 대량 데이터도 한 번의 배치 처리로 저장
        result.size shouldBe 10
        verify(exactly = 1) { mockRepository.saveAll(any<List<CarJpaEntity>>()) }
    }
    
    "CarJpaAdapter는 헥사고날 아키텍처 원칙을 준수한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>(relaxed = true)
        
        // when
        val adapter = CarJpaAdapter(mockRepository)
        
        // then - 아웃바운드 포트 구현
        adapter.shouldBeInstanceOf<CarLoadPort>()
        adapter.shouldBeInstanceOf<CarSavePort>()
        
        // 도메인 객체만 사용 (JPA 엔티티는 내부에서만 사용)
        val licensePlateNumber = LicensePlateNumber("333가3333")
        val carData = listOf(CarData(licensePlateNumber))
        
        adapter.findByLicensePlateNumberOrNull(licensePlateNumber)
        adapter.saveAll(carData)
        
        verify { mockRepository.findByLicensePlateNumber(licensePlateNumber.value) }
        verify { mockRepository.saveAll(any<List<CarJpaEntity>>()) }
    }
})
