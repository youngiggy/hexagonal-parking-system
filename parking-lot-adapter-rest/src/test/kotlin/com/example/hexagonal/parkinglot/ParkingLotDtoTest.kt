package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Instant

class ParkingLotDtoTest : StringSpec({
    "CreateParkingLotRequest가 생성된다" {
        // given
        val name = "DTO 테스트 주차장"
        val totalSpaces = 50
        
        // when
        val request = CreateParkingLotRequest(
            name = name,
            totalSpaces = totalSpaces
        )
        
        // then
        request.shouldBeInstanceOf<CreateParkingLotRequest>()
        request.name shouldBe name
        request.totalSpaces shouldBe totalSpaces
    }
    
    "CreateParkingLotRequest를 도메인 모델로 변환할 수 있다" {
        // given
        val request = CreateParkingLotRequest(
            name = "변환 테스트 주차장",
            totalSpaces = 200
        )
        
        // when
        val parkingLotName = request.toParkingLotName()
        val totalSpaces = request.toTotalSpaces()
        
        // then
        parkingLotName.shouldBeInstanceOf<ParkingLotName>()
        parkingLotName.value shouldBe request.name
        totalSpaces.shouldBeInstanceOf<ParkingSpaceCount>()
        totalSpaces.value shouldBe request.totalSpaces
    }
    
    "ParkingLotResponse가 생성된다" {
        // given
        val name = "응답 테스트 주차장"
        val totalSpaces = 100
        val availableSpaces = 80
        val occupiedSpaces = 20
        val occupancyRate = 0.2
        
        // when
        val response = ParkingLotResponse(
            name = name,
            totalSpaces = totalSpaces,
            availableSpaces = availableSpaces,
            occupiedSpaces = occupiedSpaces,
            occupancyRate = occupancyRate
        )
        
        // then
        response.shouldBeInstanceOf<ParkingLotResponse>()
        response.name shouldBe name
        response.totalSpaces shouldBe totalSpaces
        response.availableSpaces shouldBe availableSpaces
        response.occupiedSpaces shouldBe occupiedSpaces
        response.occupancyRate shouldBe occupancyRate
    }
    
    "도메인 모델을 ParkingLotResponse로 변환할 수 있다" {
        // given
        val status = ParkingLotStatus(
            name = ParkingLotName("상태 변환 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(150),
            availableSpaces = ParkingSpaceCount(120),
            occupiedSpaces = ParkingSpaceCount(30),
            occupancyRate = 0.2
        )
        
        // when
        val response = ParkingLotResponse.fromDomainModel(status)
        
        // then
        response.shouldBeInstanceOf<ParkingLotResponse>()
        response.name shouldBe status.name.value
        response.totalSpaces shouldBe status.totalSpaces.value
        response.availableSpaces shouldBe status.availableSpaces.value
        response.occupiedSpaces shouldBe status.occupiedSpaces.value
        response.occupancyRate shouldBe status.occupancyRate
    }
    
    "ParkCarRequest가 생성된다" {
        // given
        val licensePlateNumber = "123가1234"
        
        // when
        val request = ParkCarRequest(licensePlateNumber = licensePlateNumber)
        
        // then
        request.shouldBeInstanceOf<ParkCarRequest>()
        request.licensePlateNumber shouldBe licensePlateNumber
    }
    
    "ParkCarRequest를 도메인 모델로 변환할 수 있다" {
        // given
        val request = ParkCarRequest(licensePlateNumber = "456나5678")
        
        // when
        val licensePlateNumber = request.toLicensePlateNumber()
        
        // then
        licensePlateNumber.shouldBeInstanceOf<LicensePlateNumber>()
        licensePlateNumber.value shouldBe request.licensePlateNumber
    }
    
    "LeaveCarRequest가 생성된다" {
        // given
        val licensePlateNumber = "789다7890"
        
        // when
        val request = LeaveCarRequest(licensePlateNumber = licensePlateNumber)
        
        // then
        request.shouldBeInstanceOf<LeaveCarRequest>()
        request.licensePlateNumber shouldBe licensePlateNumber
    }
    
    "LeaveCarRequest를 도메인 모델로 변환할 수 있다" {
        // given
        val request = LeaveCarRequest(licensePlateNumber = "111가1111")
        
        // when
        val licensePlateNumber = request.toLicensePlateNumber()
        
        // then
        licensePlateNumber.shouldBeInstanceOf<LicensePlateNumber>()
        licensePlateNumber.value shouldBe request.licensePlateNumber
    }
    
    "ParkingRecordResponse가 생성된다" {
        // given
        val licensePlateNumber = "222나2222"
        val parkingLotName = "기록 응답 테스트 주차장"
        val parkedAt = Instant.now()
        val isParked = true
        
        // when
        val response = ParkingRecordResponse(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = parkingLotName,
            parkedAt = parkedAt,
            leftAt = null,
            isParked = isParked
        )
        
        // then
        response.shouldBeInstanceOf<ParkingRecordResponse>()
        response.licensePlateNumber shouldBe licensePlateNumber
        response.parkingLotName shouldBe parkingLotName
        response.parkedAt shouldBe parkedAt
        response.leftAt shouldBe null
        response.isParked shouldBe isParked
    }
    
    "도메인 모델을 ParkingRecordResponse로 변환할 수 있다" {
        // given
        val parkingRecord = ParkingRecord(
            licensePlateNumber = LicensePlateNumber("333다3333"),
            parkingLotName = ParkingLotName("기록 변환 테스트 주차장"),
            parkedAt = Instant.now().minusSeconds(3600)
        )
        
        // when
        val response = ParkingRecordResponse.fromDomainModel(parkingRecord)
        
        // then
        response.shouldBeInstanceOf<ParkingRecordResponse>()
        response.licensePlateNumber shouldBe parkingRecord.licensePlateNumber.value
        response.parkingLotName shouldBe parkingRecord.parkingLotName.value
        response.parkedAt shouldBe parkingRecord.parkedAt
        response.leftAt shouldBe parkingRecord.leftAt
        response.isParked shouldBe parkingRecord.isParked
    }
    
    "출차된 ParkingRecord를 ParkingRecordResponse로 변환할 수 있다" {
        // given
        val originalRecord = ParkingRecord(
            licensePlateNumber = LicensePlateNumber("444라4444"),
            parkingLotName = ParkingLotName("출차 변환 테스트 주차장"),
            parkedAt = Instant.now().minusSeconds(7200)
        )
        val leftRecord = originalRecord.leave()
        
        // when
        val response = ParkingRecordResponse.fromDomainModel(leftRecord)
        
        // then
        response.shouldBeInstanceOf<ParkingRecordResponse>()
        response.licensePlateNumber shouldBe leftRecord.licensePlateNumber.value
        response.leftAt shouldNotBe null
        response.isParked shouldBe false
    }
})
