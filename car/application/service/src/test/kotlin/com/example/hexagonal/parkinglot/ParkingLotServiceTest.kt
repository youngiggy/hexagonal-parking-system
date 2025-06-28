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

class ParkingLotServiceTest : StringSpec({
    "ParkingLotService는 새로운 주차장을 생성할 수 있다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val parkingLotName = ParkingLotName("새로운 주차장")
        val totalSpaces = ParkingSpaceCount(100)
        
        every { loadPort.existsParkingLot(parkingLotName) } returns false
        every { savePort.saveParkingLot(any()) } returnsArgument 0
        
        // when
        val createdParkingLot = service.createParkingLot(parkingLotName, totalSpaces)
        
        // then
        createdParkingLot.shouldBeInstanceOf<ParkingLot>()
        createdParkingLot.name shouldBe parkingLotName
        createdParkingLot.totalSpaces shouldBe totalSpaces
        createdParkingLot.availableSpaces shouldBe totalSpaces
        createdParkingLot.occupiedSpaces.value shouldBe 0
        
        verify(exactly = 1) { loadPort.existsParkingLot(parkingLotName) }
        verify(exactly = 1) { savePort.saveParkingLot(any()) }
    }
    
    "ParkingLotService는 이미 존재하는 주차장 생성시 예외를 발생시킨다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val parkingLotName = ParkingLotName("기존 주차장")
        val totalSpaces = ParkingSpaceCount(50)
        
        every { loadPort.existsParkingLot(parkingLotName) } returns true
        
        // when & then
        try {
            service.createParkingLot(parkingLotName, totalSpaces)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: ParkingLotAlreadyExistsException) {
            e.message shouldBe "이미 존재하는 주차장입니다: ${parkingLotName.value}"
        }
        
        verify(exactly = 1) { loadPort.existsParkingLot(parkingLotName) }
        verify(exactly = 0) { savePort.saveParkingLot(any()) }
    }
    
    "ParkingLotService는 주차장 상태를 조회할 수 있다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val parkingLotName = ParkingLotName("상태 조회 주차장")
        val parkingLot = ParkingLot(
            name = parkingLotName,
            totalSpaces = ParkingSpaceCount(200)
        )
        
        every { loadPort.loadParkingLot(parkingLotName) } returns parkingLot
        every { loadPort.loadParkedCars(parkingLotName) } returns listOf(
            ParkingRecord(
                licensePlateNumber = LicensePlateNumber("111가1111"),
                parkingLotName = parkingLotName,
                parkedAt = Instant.now().minusSeconds(1800)
            )
        )
        
        // when
        val status = service.getParkingLotStatus(parkingLotName)
        
        // then
        status.shouldBeInstanceOf<ParkingLotStatus>()
        status.name shouldBe parkingLotName
        status.totalSpaces.value shouldBe 200
        status.occupiedSpaces.value shouldBe 1
        status.availableSpaces.value shouldBe 199
        
        verify(exactly = 1) { loadPort.loadParkingLot(parkingLotName) }
        verify(exactly = 1) { loadPort.loadParkedCars(parkingLotName) }
    }
    
    "ParkingLotService는 존재하지 않는 주차장 조회시 예외를 발생시킨다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val parkingLotName = ParkingLotName("존재하지 않는 주차장")
        
        every { loadPort.loadParkingLot(parkingLotName) } returns null
        
        // when & then
        try {
            service.getParkingLotStatus(parkingLotName)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: ParkingLotNotFoundException) {
            e.message shouldBe "주차장을 찾을 수 없습니다: ${parkingLotName.value}"
        }
        
        verify(exactly = 1) { loadPort.loadParkingLot(parkingLotName) }
    }
    
    "ParkingLotService는 차량을 주차할 수 있다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val parkingLotName = ParkingLotName("주차 테스트 주차장")
        val licensePlateNumber = LicensePlateNumber("123가1234")
        val parkingLot = ParkingLot(
            name = parkingLotName,
            totalSpaces = ParkingSpaceCount(100)
        )
        
        every { loadPort.loadParkingLot(parkingLotName) } returns parkingLot
        every { loadPort.loadParkingRecord(licensePlateNumber) } returns null
        every { loadPort.loadParkedCars(parkingLotName) } returns emptyList()
        every { savePort.saveParkingRecord(any()) } returnsArgument 0
        
        // when
        val parkingRecord = service.parkCar(parkingLotName, licensePlateNumber)
        
        // then
        parkingRecord.shouldBeInstanceOf<ParkingRecord>()
        parkingRecord.licensePlateNumber shouldBe licensePlateNumber
        parkingRecord.parkingLotName shouldBe parkingLotName
        parkingRecord.parkedAt shouldNotBe null
        parkingRecord.leftAt shouldBe null
        parkingRecord.isParked shouldBe true
        
        verify(exactly = 1) { loadPort.loadParkingLot(parkingLotName) }
        verify(exactly = 1) { loadPort.loadParkingRecord(licensePlateNumber) }
        verify(exactly = 1) { loadPort.loadParkedCars(parkingLotName) }
        verify(exactly = 1) { savePort.saveParkingRecord(any()) }
    }
    
    "ParkingLotService는 이미 주차된 차량 주차시 예외를 발생시킨다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val parkingLotName = ParkingLotName("중복 주차 테스트 주차장")
        val licensePlateNumber = LicensePlateNumber("456나5678")
        val parkingLot = ParkingLot(
            name = parkingLotName,
            totalSpaces = ParkingSpaceCount(100)
        )
        val existingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = ParkingLotName("다른 주차장"),
            parkedAt = Instant.now().minusSeconds(3600)
        )
        
        every { loadPort.loadParkingLot(parkingLotName) } returns parkingLot
        every { loadPort.loadParkingRecord(licensePlateNumber) } returns existingRecord
        every { loadPort.loadParkedCars(parkingLotName) } returns emptyList()
        
        // when & then
        try {
            service.parkCar(parkingLotName, licensePlateNumber)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: CarAlreadyParkedException) {
            e.message shouldBe "이미 주차된 차량입니다: ${licensePlateNumber.value}"
        }
        
        verify(exactly = 1) { loadPort.loadParkingLot(parkingLotName) }
        verify(exactly = 1) { loadPort.loadParkingRecord(licensePlateNumber) }
        verify(exactly = 0) { loadPort.loadParkedCars(parkingLotName) }
        verify(exactly = 0) { savePort.saveParkingRecord(any()) }
    }
    
    "ParkingLotService는 차량을 출차할 수 있다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val licensePlateNumber = LicensePlateNumber("789다7890")
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = ParkingLotName("출차 테스트 주차장"),
            parkedAt = Instant.now().minusSeconds(3600)
        )
        
        every { loadPort.loadParkingRecord(licensePlateNumber) } returns parkingRecord
        every { savePort.updateParkingRecord(any()) } returnsArgument 0
        
        // when
        val leftRecord = service.leaveCar(licensePlateNumber)
        
        // then
        leftRecord.shouldBeInstanceOf<ParkingRecord>()
        leftRecord.licensePlateNumber shouldBe licensePlateNumber
        leftRecord.parkedAt shouldBe parkingRecord.parkedAt
        leftRecord.leftAt shouldNotBe null
        leftRecord.isParked shouldBe false
        
        verify(exactly = 1) { loadPort.loadParkingRecord(licensePlateNumber) }
        verify(exactly = 1) { savePort.updateParkingRecord(any()) }
    }
    
    "ParkingLotService는 주차되지 않은 차량 출차시 예외를 발생시킨다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val licensePlateNumber = LicensePlateNumber("999가9999")
        
        every { loadPort.loadParkingRecord(licensePlateNumber) } returns null
        
        // when & then
        try {
            service.leaveCar(licensePlateNumber)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: CarNotParkedException) {
            e.message shouldBe "주차되지 않은 차량입니다: ${licensePlateNumber.value}"
        }
        
        verify(exactly = 1) { loadPort.loadParkingRecord(licensePlateNumber) }
        verify(exactly = 0) { savePort.updateParkingRecord(any()) }
    }
    
    "ParkingLotService는 주차된 차량 목록을 조회할 수 있다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val parkingLotName = ParkingLotName("차량 목록 조회 주차장")
        val parkedCars = listOf(
            ParkingRecord(
                licensePlateNumber = LicensePlateNumber("111가1111"),
                parkingLotName = parkingLotName,
                parkedAt = Instant.now().minusSeconds(1800)
            ),
            ParkingRecord(
                licensePlateNumber = LicensePlateNumber("222나2222"),
                parkingLotName = parkingLotName,
                parkedAt = Instant.now().minusSeconds(3600)
            )
        )
        
        every { loadPort.loadParkedCars(parkingLotName) } returns parkedCars
        
        // when
        val result = service.getParkedCars(parkingLotName)
        
        // then
        result.size shouldBe 2
        result shouldBe parkedCars
        result.forEach { record ->
            record.parkingLotName shouldBe parkingLotName
            record.isParked shouldBe true
        }
        
        verify(exactly = 1) { loadPort.loadParkedCars(parkingLotName) }
    }
    
    "ParkingLotService는 특정 차량의 주차 기록을 조회할 수 있다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val licensePlateNumber = LicensePlateNumber("555마5555")
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = ParkingLotName("기록 조회 주차장"),
            parkedAt = Instant.now().minusSeconds(7200)
        )
        
        every { loadPort.loadParkingRecord(licensePlateNumber) } returns parkingRecord
        
        // when
        val result = service.findParkingRecord(licensePlateNumber)
        
        // then
        result shouldNotBe null
        result!!.licensePlateNumber shouldBe licensePlateNumber
        result.isParked shouldBe true
        
        verify(exactly = 1) { loadPort.loadParkingRecord(licensePlateNumber) }
    }
    
    "ParkingLotService는 존재하지 않는 차량 기록 조회시 null을 반환한다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val licensePlateNumber = LicensePlateNumber("000가0000")
        
        every { loadPort.loadParkingRecord(licensePlateNumber) } returns null
        
        // when
        val result = service.findParkingRecord(licensePlateNumber)
        
        // then
        result shouldBe null
        
        verify(exactly = 1) { loadPort.loadParkingRecord(licensePlateNumber) }
    }
})
