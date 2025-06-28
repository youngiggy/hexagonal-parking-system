package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant

class ParkingLotAdapterUnitTest : StringSpec({
    
    "ParkingLot JPA와 REST 어댑터가 함께 동작한다" {
        // given - JPA 어댑터 설정
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val jpaAdapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        // given - 서비스 설정
        val service = ParkingLotService(jpaAdapter, jpaAdapter)
        
        // given - REST 어댑터 설정
        val restApi = ParkingLotRestApi(service, service)
        
        // given - Mock 데이터 설정
        val parkingLotName = ParkingLotName("어댑터테스트주차장")
        val parkingLot = ParkingLot(name = parkingLotName, totalSpaces = ParkingSpaceCount(10))
        val parkingLotEntity = ParkingLotJpaEntity.fromDomainModel(parkingLot).apply { id = 1L }
        
        every { parkingLotRepository.existsByName(parkingLotName.value) } returns false
        every { parkingLotRepository.save(any()) } returns parkingLotEntity
        
        // when - REST API를 통한 주차장 생성
        val createRequest = CreateParkingLotRequest(
            name = parkingLotName.value,
            totalSpaces = 10
        )
        val createResponse = restApi.createParkingLot(createRequest)
        
        // then
        createResponse.statusCode.value() shouldBe 201
        createResponse.body shouldNotBe null
        createResponse.body!!.name shouldBe parkingLotName.value
        createResponse.body!!.totalSpaces shouldBe 10
        
        verify(exactly = 1) { parkingLotRepository.existsByName(parkingLotName.value) }
        verify(exactly = 1) { parkingLotRepository.save(any()) }
    }
    
    "ParkingLot 서비스와 어댑터 계층이 올바르게 연동된다" {
        // given - JPA 어댑터 설정
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val jpaAdapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        // given - 서비스 설정
        val service = ParkingLotService(jpaAdapter, jpaAdapter)
        
        // given - Mock 데이터 설정
        val parkingLotName = ParkingLotName("서비스연동테스트주차장")
        val licensePlateNumber = LicensePlateNumber("123가1234")
        
        val parkingLot = ParkingLot(name = parkingLotName, totalSpaces = ParkingSpaceCount(5))
        val parkingLotEntity = ParkingLotJpaEntity.fromDomainModel(parkingLot).apply { id = 1L }
        
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = parkingLotName,
            parkedAt = Instant.now()
        )
        val parkingRecordEntity = ParkingRecordJpaEntity.fromDomainModel(parkingRecord).apply { id = 1L }
        
        // 주차장 생성 Mock
        every { parkingLotRepository.existsByName(parkingLotName.value) } returns false
        every { parkingLotRepository.save(any()) } returns parkingLotEntity
        
        // 주차장 조회 Mock
        every { parkingLotRepository.findByName(parkingLotName.value) } returns parkingLotEntity
        every { parkingRecordRepository.findByParkingLotNameAndLeftAtIsNull(parkingLotName.value) } returns emptyList()
        
        // 차량 주차 Mock
        every { parkingRecordRepository.findByLicensePlateNumberAndLeftAtIsNull(licensePlateNumber.value) } returns null
        every { parkingRecordRepository.findByParkingLotNameAndLeftAtIsNull(parkingLotName.value) } returns emptyList()
        every { parkingRecordRepository.save(any()) } returns parkingRecordEntity
        
        // when & then - 주차장 생성
        val createdParkingLot = service.createParkingLot(parkingLotName, ParkingSpaceCount(5))
        createdParkingLot.name shouldBe parkingLotName
        createdParkingLot.totalSpaces.value shouldBe 5
        
        // when & then - 주차장 상태 조회
        val status = service.getParkingLotStatus(parkingLotName)
        status.name shouldBe parkingLotName
        status.totalSpaces.value shouldBe 5
        status.occupiedSpaces.value shouldBe 0
        
        // when & then - 차량 주차
        val parkedRecord = service.parkCar(parkingLotName, licensePlateNumber)
        parkedRecord.licensePlateNumber shouldBe licensePlateNumber
        parkedRecord.isParked shouldBe true
        
        // 검증 - 기본적인 동작 확인
        verify(atLeast = 1) { parkingLotRepository.existsByName(any()) }
        verify(atLeast = 1) { parkingLotRepository.save(any()) }
        verify(atLeast = 1) { parkingRecordRepository.save(any()) }
    }
    
    "DTO 변환이 올바르게 동작한다" {
        // given
        val parkingLot = ParkingLot(
            name = ParkingLotName("DTO테스트주차장"),
            totalSpaces = ParkingSpaceCount(20)
        )
        val status = parkingLot.getStatus()
        
        val parkingRecord = ParkingRecord(
            licensePlateNumber = LicensePlateNumber("456나5678"),
            parkingLotName = ParkingLotName("DTO테스트주차장"),
            parkedAt = Instant.now()
        )
        
        // when - 도메인 모델을 DTO로 변환
        val parkingLotResponse = ParkingLotResponse.fromDomainModel(status)
        val parkingRecordResponse = ParkingRecordResponse.fromDomainModel(parkingRecord)
        
        // then
        parkingLotResponse.shouldBeInstanceOf<ParkingLotResponse>()
        parkingLotResponse.name shouldBe "DTO테스트주차장"
        parkingLotResponse.totalSpaces shouldBe 20
        
        parkingRecordResponse.shouldBeInstanceOf<ParkingRecordResponse>()
        parkingRecordResponse.licensePlateNumber shouldBe "456나5678"
        parkingRecordResponse.parkingLotName shouldBe "DTO테스트주차장"
        parkingRecordResponse.isParked shouldBe true
        
        // when - DTO를 도메인 모델로 변환
        val createRequest = CreateParkingLotRequest(name = "새주차장", totalSpaces = 30)
        val parkRequest = ParkCarRequest(licensePlateNumber = "789다7890")
        val leaveRequest = LeaveCarRequest(licensePlateNumber = "789다7890")
        
        // then
        createRequest.toParkingLotName().value shouldBe "새주차장"
        createRequest.toTotalSpaces().value shouldBe 30
        
        parkRequest.toLicensePlateNumber().value shouldBe "789다7890"
        leaveRequest.toLicensePlateNumber().value shouldBe "789다7890"
    }
    
    "예외 처리가 올바르게 동작한다" {
        // given - JPA 어댑터 설정
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val jpaAdapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        // given - 서비스 설정
        val service = ParkingLotService(jpaAdapter, jpaAdapter)
        
        // given - REST 어댑터 설정
        val restApi = ParkingLotRestApi(service, service)
        
        // given - 중복 주차장 생성 시나리오
        val parkingLotName = ParkingLotName("중복주차장")
        every { parkingLotRepository.existsByName(parkingLotName.value) } returns true
        
        // when - 중복 주차장 생성 시도
        val createRequest = CreateParkingLotRequest(
            name = parkingLotName.value,
            totalSpaces = 10
        )
        val response = restApi.createParkingLot(createRequest)
        
        // then
        response.statusCode.value() shouldBe 409 // CONFLICT
        response.body shouldBe null
        
        verify(exactly = 1) { parkingLotRepository.existsByName(parkingLotName.value) }
        verify(exactly = 0) { parkingLotRepository.save(any()) }
    }
})
