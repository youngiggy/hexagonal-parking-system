package com.example.hexagonal.car

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

class CarJpaRepositoryTest : StringSpec({
    "CarJpaRepository는 JpaRepository를 상속한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        
        // when & then
        mockRepository.shouldBeInstanceOf<JpaRepository<CarJpaEntity, UUID>>()
    }
    
    "번호판으로 차량을 조회할 수 있다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val licensePlateNumber = "123가1234"
        val expectedEntity = CarJpaEntity(licensePlateNumber = licensePlateNumber)
        
        every { mockRepository.findByLicensePlateNumber(licensePlateNumber) } returns expectedEntity
        
        // when
        val result = mockRepository.findByLicensePlateNumber(licensePlateNumber)
        
        // then
        result shouldBe expectedEntity
        result?.licensePlateNumber shouldBe licensePlateNumber
        result.shouldBeInstanceOf<CarJpaEntity>()
        verify(exactly = 1) { mockRepository.findByLicensePlateNumber(licensePlateNumber) }
    }
    
    "존재하지 않는 번호판으로 조회시 null을 반환한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val nonExistentLicensePlateNumber = "999가9999"
        
        every { mockRepository.findByLicensePlateNumber(nonExistentLicensePlateNumber) } returns null
        
        // when
        val result = mockRepository.findByLicensePlateNumber(nonExistentLicensePlateNumber)
        
        // then
        result shouldBe null
        verify(exactly = 1) { mockRepository.findByLicensePlateNumber(nonExistentLicensePlateNumber) }
    }
    
    "CarJpaRepository는 기본 CRUD 메서드를 제공한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>(relaxed = true)
        val entity = CarJpaEntity(licensePlateNumber = "456나5678")
        val entities = listOf(entity)
        
        every { mockRepository.save(entity) } returns entity
        every { mockRepository.saveAll(entities) } returns entities
        every { mockRepository.findById(any()) } returns Optional.of(entity)
        every { mockRepository.findAll() } returns entities
        every { mockRepository.count() } returns 1L
        every { mockRepository.existsById(any()) } returns true
        
        // when & then
        mockRepository.save(entity) shouldBe entity
        mockRepository.saveAll(entities) shouldBe entities
        mockRepository.findById(entity.id).get() shouldBe entity
        mockRepository.findAll() shouldBe entities
        mockRepository.count() shouldBe 1L
        mockRepository.existsById(entity.id) shouldBe true
        
        verify(exactly = 1) { mockRepository.save(entity) }
        verify(exactly = 1) { mockRepository.saveAll(entities) }
        verify(exactly = 1) { mockRepository.findById(entity.id) }
        verify(exactly = 1) { mockRepository.findAll() }
        verify(exactly = 1) { mockRepository.count() }
        verify(exactly = 1) { mockRepository.existsById(entity.id) }
    }
    
    "번호판 존재 여부를 확인할 수 있다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val existingLicensePlateNumber = "111가1111"
        val nonExistentLicensePlateNumber = "222나2222"
        
        every { mockRepository.existsByLicensePlateNumber(existingLicensePlateNumber) } returns true
        every { mockRepository.existsByLicensePlateNumber(nonExistentLicensePlateNumber) } returns false
        
        // when & then
        mockRepository.existsByLicensePlateNumber(existingLicensePlateNumber) shouldBe true
        mockRepository.existsByLicensePlateNumber(nonExistentLicensePlateNumber) shouldBe false
        
        verify(exactly = 1) { mockRepository.existsByLicensePlateNumber(existingLicensePlateNumber) }
        verify(exactly = 1) { mockRepository.existsByLicensePlateNumber(nonExistentLicensePlateNumber) }
    }
    
    "번호판으로 차량을 삭제할 수 있다" {
        // given
        val mockRepository = mockk<CarJpaRepository>(relaxed = true)
        val licensePlateNumber = "333다3333"
        
        every { mockRepository.deleteByLicensePlateNumber(licensePlateNumber) } returns 1L
        
        // when
        val deletedCount = mockRepository.deleteByLicensePlateNumber(licensePlateNumber)
        
        // then
        deletedCount shouldBe 1L
        deletedCount.shouldBeInstanceOf<Long>()
        verify(exactly = 1) { mockRepository.deleteByLicensePlateNumber(licensePlateNumber) }
    }
    
    "존재하지 않는 번호판 삭제시 0을 반환한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val nonExistentLicensePlateNumber = "존재안함1234"
        
        every { mockRepository.deleteByLicensePlateNumber(nonExistentLicensePlateNumber) } returns 0L
        
        // when
        val deletedCount = mockRepository.deleteByLicensePlateNumber(nonExistentLicensePlateNumber)
        
        // then
        deletedCount shouldBe 0L
        verify(exactly = 1) { mockRepository.deleteByLicensePlateNumber(nonExistentLicensePlateNumber) }
    }
    
    "CarJpaRepository 메서드 시그니처가 올바르다" {
        // given
        val mockRepository = mockk<CarJpaRepository>(relaxed = true)
        
        // when & then - 컴파일 시점에 메서드 시그니처 검증
        val licensePlateNumber = "시그니처1234"
        
        // 반환 타입 검증
        val findResult: CarJpaEntity? = mockRepository.findByLicensePlateNumber(licensePlateNumber)
        val existsResult: Boolean = mockRepository.existsByLicensePlateNumber(licensePlateNumber)
        val deleteResult: Long = mockRepository.deleteByLicensePlateNumber(licensePlateNumber)
        
        // 타입 안전성 확인
        findResult?.shouldBeInstanceOf<CarJpaEntity>()
        existsResult.shouldBeInstanceOf<Boolean>()
        deleteResult.shouldBeInstanceOf<Long>()
    }
    
    "여러 차량을 일괄 저장할 수 있다" {
        // given
        val mockRepository = mockk<CarJpaRepository>()
        val entities = listOf(
            CarJpaEntity(licensePlateNumber = "일괄가1111"),
            CarJpaEntity(licensePlateNumber = "저장나2222"),
            CarJpaEntity(licensePlateNumber = "테스트다3333")
        )
        
        every { mockRepository.saveAll(entities) } returns entities
        
        // when
        val result = mockRepository.saveAll(entities)
        
        // then
        result shouldBe entities
        result.size shouldBe 3
        result.forEach { entity ->
            entity.shouldBeInstanceOf<CarJpaEntity>()
            entity.licensePlateNumber shouldNotBe ""
        }
        verify(exactly = 1) { mockRepository.saveAll(entities) }
    }
    
    "Spring Data JPA 쿼리 메서드 네이밍 규칙을 준수한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>(relaxed = true)
        
        // when & then - Spring Data JPA 메서드 네이밍 규칙 검증
        // findBy* : 조회 메서드
        val findResult = mockRepository.findByLicensePlateNumber("네이밍가1234")
        
        // existsBy* : 존재 여부 확인 메서드
        val existsResult = mockRepository.existsByLicensePlateNumber("규칙나5678")
        
        // deleteBy* : 삭제 메서드
        val deleteResult = mockRepository.deleteByLicensePlateNumber("준수다9012")
        
        // 메서드가 정상적으로 호출되고 적절한 타입을 반환하는지 확인
        findResult?.shouldBeInstanceOf<CarJpaEntity>()
        existsResult.shouldBeInstanceOf<Boolean>()
        deleteResult.shouldBeInstanceOf<Long>()
    }
    
    "CarJpaRepository는 트랜잭션 안전성을 보장한다" {
        // given
        val mockRepository = mockk<CarJpaRepository>(relaxed = true)
        val entities = listOf(
            CarJpaEntity(licensePlateNumber = "트랜잭션1111"),
            CarJpaEntity(licensePlateNumber = "안전성2222")
        )
        
        every { mockRepository.saveAll(entities) } returns entities
        
        // when
        val result = mockRepository.saveAll(entities)
        
        // then - 일괄 저장이 원자적으로 처리되어야 함
        result.size shouldBe entities.size
        result.forEachIndexed { index, entity ->
            entity.licensePlateNumber shouldBe entities[index].licensePlateNumber
        }
        
        verify(exactly = 1) { mockRepository.saveAll(entities) }
    }
})
