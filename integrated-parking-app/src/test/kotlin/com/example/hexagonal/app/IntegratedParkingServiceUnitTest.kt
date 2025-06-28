package com.example.hexagonal.app

import com.example.hexagonal.car.*
import com.example.hexagonal.parkinglot.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.util.*

class IntegratedParkingServiceUnitTest : StringSpec({
    
    "IntegratedParkingService가 생성된다" {
        // given
        val carCommandUseCase = mockk<CarCommandUseCase>()
        val carQueryUseCase = mockk<CarQueryUseCase>()
        val parkingLotCommandUseCase = mockk<ParkingLotCommandUseCase>()
        val parkingLotQueryUseCase = mockk<ParkingLotQueryUseCase>()
        
        // when
        val service = IntegratedParkingService(
            carCommandUseCase,
            carQueryUseCase,
            parkingLotCommandUseCase,
            parkingLotQueryUseCase
        )
        
        // then
        service.shouldBeInstanceOf<IntegratedParkingService>()
    }
    
    "registerCarAndPark이 차량 등록과 주차를 처리한다" {
        // given
        val carCommandUseCase = mockk<CarCommandUseCase>()
        val carQueryUseCase = mockk<CarQueryUseCase>()
        val parkingLotCommandUseCase = mockk<ParkingLotCommandUseCase>()
        val parkingLotQueryUseCase = mockk<ParkingLotQueryUseCase>()
        
        val service = IntegratedParkingService(
            carCommandUseCase,
            carQueryUseCase,
            parkingLotCommandUseCase,
            parkingLotQueryUseCase
        )
        
        val licensePlateNumber = LicensePlateNumber("123가1234")
        val parkingLotName = ParkingLotName("테스트주차장")
        
        // Mock 설정
        val carEntity = CarEntity(
            licencePlateNumber = licensePlateNumber,
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        val parkingLot = ParkingLot(
            name = parkingLotName,
            totalSpaces = ParkingSpaceCount(10)
        )
        
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = parkingLotName,
            parkedAt = Instant.now()
        )
        
        every { carCommandUseCase.bulkCreateCar(any()) } returns listOf(carEntity)
        every { parkingLotQueryUseCase.getParkingLotStatus(parkingLotName) } throws ParkingLotNotFoundException("주차장 없음")
        every { parkingLotCommandUseCase.createParkingLot(parkingLotName, any()) } returns parkingLot
        every { parkingLotCommandUseCase.parkCar(parkingLotName, licensePlateNumber) } returns parkingRecord
        
        // when
        val result = service.registerCarAndPark(
            licensePlateNumber = licensePlateNumber,
            model = "테스트카",
            color = "화이트",
            parkingLotName = parkingLotName,
            totalSpaces = ParkingSpaceCount(10)
        )
        
        // then
        result.car.licencePlateNumber shouldBe licensePlateNumber
        result.parkingRecord.licensePlateNumber shouldBe licensePlateNumber
        result.parkingRecord.isParked shouldBe true
        result.parkingLot shouldBe parkingLot
        
        verify(exactly = 1) { carCommandUseCase.bulkCreateCar(any()) }
        verify(exactly = 1) { parkingLotCommandUseCase.createParkingLot(parkingLotName, any()) }
        verify(exactly = 1) { parkingLotCommandUseCase.parkCar(parkingLotName, licensePlateNumber) }
    }
    
    "leaveAndUnregisterCar가 출차와 차량 조회를 처리한다" {
        // given
        val carCommandUseCase = mockk<CarCommandUseCase>()
        val carQueryUseCase = mockk<CarQueryUseCase>()
        val parkingLotCommandUseCase = mockk<ParkingLotCommandUseCase>()
        val parkingLotQueryUseCase = mockk<ParkingLotQueryUseCase>()
        
        val service = IntegratedParkingService(
            carCommandUseCase,
            carQueryUseCase,
            parkingLotCommandUseCase,
            parkingLotQueryUseCase
        )
        
        val licensePlateNumber = LicensePlateNumber("456나5678")
        
        // Mock 설정
        val carEntity = CarEntity(
            licencePlateNumber = licensePlateNumber,
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = ParkingLotName("테스트주차장"),
            parkedAt = Instant.now().minusSeconds(3600),
            leftAt = Instant.now()
        )
        
        every { parkingLotCommandUseCase.leaveCar(licensePlateNumber) } returns parkingRecord
        every { carQueryUseCase.getByLicensePlateNumber(licensePlateNumber) } returns carEntity
        
        // when
        val result = service.leaveAndUnregisterCar(licensePlateNumber)
        
        // then
        result.car.licencePlateNumber shouldBe licensePlateNumber
        result.parkingRecord.licensePlateNumber shouldBe licensePlateNumber
        result.parkingRecord.isParked shouldBe false
        result.parkingRecord.leftAt shouldNotBe null
        
        verify(exactly = 1) { parkingLotCommandUseCase.leaveCar(licensePlateNumber) }
        verify(exactly = 1) { carQueryUseCase.getByLicensePlateNumber(licensePlateNumber) }
    }
    
    "getRegisteredCarsInParkingLot이 주차장 내 등록된 차량을 조회한다" {
        // given
        val carCommandUseCase = mockk<CarCommandUseCase>()
        val carQueryUseCase = mockk<CarQueryUseCase>()
        val parkingLotCommandUseCase = mockk<ParkingLotCommandUseCase>()
        val parkingLotQueryUseCase = mockk<ParkingLotQueryUseCase>()
        
        val service = IntegratedParkingService(
            carCommandUseCase,
            carQueryUseCase,
            parkingLotCommandUseCase,
            parkingLotQueryUseCase
        )
        
        val parkingLotName = ParkingLotName("조회테스트주차장")
        val licensePlateNumber1 = LicensePlateNumber("111가1111")
        val licensePlateNumber2 = LicensePlateNumber("222나2222")
        
        // Mock 설정
        val parkingRecords = listOf(
            ParkingRecord(
                licensePlateNumber = licensePlateNumber1,
                parkingLotName = parkingLotName,
                parkedAt = Instant.now()
            ),
            ParkingRecord(
                licensePlateNumber = licensePlateNumber2,
                parkingLotName = parkingLotName,
                parkedAt = Instant.now()
            )
        )
        
        val carEntity1 = CarEntity(
            licencePlateNumber = licensePlateNumber1,
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        val carEntity2 = CarEntity(
            licencePlateNumber = licensePlateNumber2,
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        every { parkingLotQueryUseCase.getParkedCars(parkingLotName) } returns parkingRecords
        every { carQueryUseCase.getByLicensePlateNumber(licensePlateNumber1) } returns carEntity1
        every { carQueryUseCase.getByLicensePlateNumber(licensePlateNumber2) } returns carEntity2
        
        // when
        val result = service.getRegisteredCarsInParkingLot(parkingLotName)
        
        // then
        result.size shouldBe 2
        result[0].car.licencePlateNumber shouldBe licensePlateNumber1
        result[1].car.licencePlateNumber shouldBe licensePlateNumber2
        
        verify(exactly = 1) { parkingLotQueryUseCase.getParkedCars(parkingLotName) }
        verify(exactly = 1) { carQueryUseCase.getByLicensePlateNumber(licensePlateNumber1) }
        verify(exactly = 1) { carQueryUseCase.getByLicensePlateNumber(licensePlateNumber2) }
    }
})
