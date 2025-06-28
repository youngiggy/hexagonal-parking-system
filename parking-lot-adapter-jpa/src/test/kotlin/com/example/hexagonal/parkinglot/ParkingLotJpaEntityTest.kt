package com.example.hexagonal.parkinglot

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Instant

class ParkingLotJpaEntityTest : StringSpec({
    "ParkingLotJpaEntity가 생성된다" {
        // given
        val name = "테스트 주차장"
        val totalSpaces = 100
        
        // when
        val entity = ParkingLotJpaEntity(
            name = name,
            totalSpaces = totalSpaces
        )
        
        // then
        entity.shouldBeInstanceOf<ParkingLotJpaEntity>()
        entity.name shouldBe name
        entity.totalSpaces shouldBe totalSpaces
        entity.id shouldBe null // 저장 전에는 null
    }
    
    "ParkingLotJpaEntity를 도메인 모델로 변환할 수 있다" {
        // given
        val entity = ParkingLotJpaEntity(
            name = "변환 테스트 주차장",
            totalSpaces = 50
        )
        entity.id = 1L
        
        // when
        val domainModel = entity.toDomainModel()
        
        // then
        domainModel.shouldBeInstanceOf<ParkingLot>()
        domainModel.name.value shouldBe entity.name
        domainModel.totalSpaces.value shouldBe entity.totalSpaces
    }
    
    "도메인 모델을 ParkingLotJpaEntity로 변환할 수 있다" {
        // given
        val domainModel = ParkingLot(
            name = ParkingLotName("도메인 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(200)
        )
        
        // when
        val entity = ParkingLotJpaEntity.fromDomainModel(domainModel)
        
        // then
        entity.shouldBeInstanceOf<ParkingLotJpaEntity>()
        entity.name shouldBe domainModel.name.value
        entity.totalSpaces shouldBe domainModel.totalSpaces.value
        entity.id shouldBe null
    }
    
    "ParkingRecordJpaEntity가 생성된다" {
        // given
        val licensePlateNumber = "123가1234"
        val parkingLotName = "기록 테스트 주차장"
        val parkedAt = Instant.now()
        
        // when
        val entity = ParkingRecordJpaEntity(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = parkingLotName,
            parkedAt = parkedAt
        )
        
        // then
        entity.shouldBeInstanceOf<ParkingRecordJpaEntity>()
        entity.licensePlateNumber shouldBe licensePlateNumber
        entity.parkingLotName shouldBe parkingLotName
        entity.parkedAt shouldBe parkedAt
        entity.leftAt shouldBe null
        entity.id shouldBe null
    }
    
    "ParkingRecordJpaEntity를 도메인 모델로 변환할 수 있다" {
        // given
        val entity = ParkingRecordJpaEntity(
            licensePlateNumber = "456나5678",
            parkingLotName = "기록 변환 테스트 주차장",
            parkedAt = Instant.now().minusSeconds(3600)
        )
        entity.id = 1L
        
        // when
        val domainModel = entity.toDomainModel()
        
        // then
        domainModel.shouldBeInstanceOf<ParkingRecord>()
        domainModel.licensePlateNumber.value shouldBe entity.licensePlateNumber
        domainModel.parkingLotName.value shouldBe entity.parkingLotName
        domainModel.parkedAt shouldBe entity.parkedAt
        domainModel.leftAt shouldBe entity.leftAt
        domainModel.isParked shouldBe true
    }
    
    "도메인 모델을 ParkingRecordJpaEntity로 변환할 수 있다" {
        // given
        val domainModel = ParkingRecord(
            licensePlateNumber = com.example.hexagonal.car.LicensePlateNumber("789다7890"),
            parkingLotName = ParkingLotName("도메인 기록 테스트 주차장"),
            parkedAt = Instant.now().minusSeconds(1800)
        )
        
        // when
        val entity = ParkingRecordJpaEntity.fromDomainModel(domainModel)
        
        // then
        entity.shouldBeInstanceOf<ParkingRecordJpaEntity>()
        entity.licensePlateNumber shouldBe domainModel.licensePlateNumber.value
        entity.parkingLotName shouldBe domainModel.parkingLotName.value
        entity.parkedAt shouldBe domainModel.parkedAt
        entity.leftAt shouldBe domainModel.leftAt
        entity.id shouldBe null
    }
    
    "출차된 ParkingRecordJpaEntity를 도메인 모델로 변환할 수 있다" {
        // given
        val leftAt = Instant.now()
        val entity = ParkingRecordJpaEntity(
            licensePlateNumber = "111가1111",
            parkingLotName = "출차 테스트 주차장",
            parkedAt = Instant.now().minusSeconds(7200),
            leftAt = leftAt
        )
        entity.id = 1L
        
        // when
        val domainModel = entity.toDomainModel()
        
        // then
        domainModel.shouldBeInstanceOf<ParkingRecord>()
        domainModel.licensePlateNumber.value shouldBe entity.licensePlateNumber
        domainModel.leftAt shouldBe leftAt
        domainModel.isParked shouldBe false
    }
})
