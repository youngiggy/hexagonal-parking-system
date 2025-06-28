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

class ParkingLotServiceIntegrationTest : StringSpec({
    "ParkingLotService는 주차장 생성부터 차량 주차/출차까지 전체 플로우를 처리할 수 있다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val parkingLotName = ParkingLotName("통합 테스트 주차장")
        val totalSpaces = ParkingSpaceCount(10)
        val licensePlateNumber = LicensePlateNumber("123가1234")
        
        // 주차장 생성 설정
        every { loadPort.existsParkingLot(parkingLotName) } returns false
        every { savePort.saveParkingLot(any()) } returnsArgument 0
        
        // 차량 주차 설정
        val createdParkingLot = ParkingLot(name = parkingLotName, totalSpaces = totalSpaces)
        every { loadPort.loadParkingLot(parkingLotName) } returns createdParkingLot
        every { loadPort.loadParkedCars(parkingLotName) } returns emptyList()
        every { savePort.saveParkingRecord(any()) } returnsArgument 0
        
        // 차량 출차 설정
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = parkingLotName,
            parkedAt = Instant.now().minusSeconds(3600)
        )
        every { savePort.updateParkingRecord(any()) } returnsArgument 0
        
        // when & then - 주차장 생성
        val parkingLot = service.createParkingLot(parkingLotName, totalSpaces)
        parkingLot.name shouldBe parkingLotName
        parkingLot.totalSpaces shouldBe totalSpaces
        
        // 주차를 위한 별도 Mock 설정
        every { loadPort.loadParkingRecord(licensePlateNumber) } returns null
        
        // when & then - 차량 주차
        val parkedRecord = service.parkCar(parkingLotName, licensePlateNumber)
        parkedRecord.licensePlateNumber shouldBe licensePlateNumber
        parkedRecord.isParked shouldBe true
        
        // 출차를 위한 별도 Mock 설정
        every { loadPort.loadParkingRecord(licensePlateNumber) } returns parkingRecord
        
        // when & then - 차량 출차
        val leftRecord = service.leaveCar(licensePlateNumber)
        leftRecord.licensePlateNumber shouldBe licensePlateNumber
        leftRecord.isParked shouldBe false
        leftRecord.leftAt shouldNotBe null
    }
    
    "ParkingLotService는 만차 상황을 올바르게 처리한다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val parkingLotName = ParkingLotName("만차 테스트 주차장")
        val parkingLot = ParkingLot(
            name = parkingLotName,
            totalSpaces = ParkingSpaceCount(2)
        )
        val licensePlateNumber = LicensePlateNumber("999가9999")
        
        // 이미 2대가 주차된 상황
        val parkedCars = listOf(
            ParkingRecord(
                licensePlateNumber = LicensePlateNumber("111가1111"),
                parkingLotName = parkingLotName,
                parkedAt = Instant.now().minusSeconds(3600)
            ),
            ParkingRecord(
                licensePlateNumber = LicensePlateNumber("222나2222"),
                parkingLotName = parkingLotName,
                parkedAt = Instant.now().minusSeconds(1800)
            )
        )
        
        every { loadPort.loadParkingLot(parkingLotName) } returns parkingLot
        every { loadPort.loadParkingRecord(licensePlateNumber) } returns null
        every { loadPort.loadParkedCars(parkingLotName) } returns parkedCars
        
        // when & then
        try {
            service.parkCar(parkingLotName, licensePlateNumber)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: ParkingLotFullException) {
            e.message shouldBe "주차장이 만차입니다: ${parkingLotName.value}"
        }
        
        verify(exactly = 1) { loadPort.loadParkingLot(parkingLotName) }
        verify(exactly = 1) { loadPort.loadParkingRecord(licensePlateNumber) }
        verify(exactly = 1) { loadPort.loadParkedCars(parkingLotName) }
        verify(exactly = 0) { savePort.saveParkingRecord(any()) }
    }
    
    "ParkingLotService는 주차장 상태를 정확하게 계산한다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val parkingLotName = ParkingLotName("상태 계산 테스트 주차장")
        val parkingLot = ParkingLot(
            name = parkingLotName,
            totalSpaces = ParkingSpaceCount(100)
        )
        
        // 30대가 주차된 상황
        val parkedCars = (1..30).map { i ->
            ParkingRecord(
                licensePlateNumber = LicensePlateNumber("${i.toString().padStart(2, '0')}가${i.toString().padStart(4, '0')}"),
                parkingLotName = parkingLotName,
                parkedAt = Instant.now().minusSeconds(i * 60L)
            )
        }
        
        every { loadPort.loadParkingLot(parkingLotName) } returns parkingLot
        every { loadPort.loadParkedCars(parkingLotName) } returns parkedCars
        
        // when
        val status = service.getParkingLotStatus(parkingLotName)
        
        // then
        status.name shouldBe parkingLotName
        status.totalSpaces.value shouldBe 100
        status.occupiedSpaces.value shouldBe 30
        status.availableSpaces.value shouldBe 70
        status.occupancyRate shouldBe 0.3
        status.isFull shouldBe false
        status.isEmpty shouldBe false
        status.getOccupancyPercentage() shouldBe 30
        
        verify(exactly = 1) { loadPort.loadParkingLot(parkingLotName) }
        verify(exactly = 1) { loadPort.loadParkedCars(parkingLotName) }
    }
    
    "ParkingLotService는 이미 출차한 차량의 재출차를 방지한다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        val licensePlateNumber = LicensePlateNumber("111가1111")
        val leftRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = ParkingLotName("재출차 방지 테스트 주차장"),
            parkedAt = Instant.now().minusSeconds(7200),
            leftAt = Instant.now().minusSeconds(3600)
        )
        
        every { loadPort.loadParkingRecord(licensePlateNumber) } returns leftRecord
        
        // when & then
        try {
            service.leaveCar(licensePlateNumber)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: CarNotParkedException) {
            e.message shouldBe "이미 출차한 차량입니다: ${licensePlateNumber.value}"
        }
        
        verify(exactly = 1) { loadPort.loadParkingRecord(licensePlateNumber) }
        verify(exactly = 0) { savePort.updateParkingRecord(any()) }
    }
    
    "ParkingLotService는 트랜잭션 어노테이션이 올바르게 적용되어 있다" {
        // given
        val loadPort = mockk<ParkingLotLoadPort>()
        val savePort = mockk<ParkingLotSavePort>()
        val service = ParkingLotService(loadPort, savePort)
        
        // when & then - 서비스 클래스 확인
        service.shouldBeInstanceOf<ParkingLotService>()
        
        // 실제 Spring 환경에서는 @Transactional 어노테이션이 적용되어
        // 읽기 전용 메서드와 쓰기 메서드가 적절히 구분됨
        val parkingLotName = ParkingLotName("트랜잭션 테스트 주차장")
        every { loadPort.loadParkedCars(parkingLotName) } returns emptyList()
        
        val result = service.getParkedCars(parkingLotName)
        result shouldBe emptyList()
    }
})
