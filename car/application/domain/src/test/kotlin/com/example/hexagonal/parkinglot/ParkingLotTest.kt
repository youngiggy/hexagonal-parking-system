package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ParkingLotTest : StringSpec({
    "ParkingLot이 생성된다" {
        // given
        val name = "메인 주차장"
        val totalSpaces = 100
        
        // when
        val parkingLot = ParkingLot(
            name = ParkingLotName(name),
            totalSpaces = ParkingSpaceCount(totalSpaces)
        )
        
        // then
        parkingLot.shouldBeInstanceOf<ParkingLot>()
        parkingLot.name.value shouldBe name
        parkingLot.totalSpaces.value shouldBe totalSpaces
        parkingLot.availableSpaces.value shouldBe totalSpaces
        parkingLot.occupiedSpaces.value shouldBe 0
    }
    
    "ParkingLot에 차량을 주차할 수 있다" {
        // given
        val parkingLot = ParkingLot(
            name = ParkingLotName("테스트 주차장"),
            totalSpaces = ParkingSpaceCount(10)
        )
        val licensePlateNumber = LicensePlateNumber("123가1234")
        
        // when
        val parkingRecord = parkingLot.parkCar(licensePlateNumber)
        
        // then
        parkingRecord.shouldBeInstanceOf<ParkingRecord>()
        parkingRecord.licensePlateNumber shouldBe licensePlateNumber
        parkingRecord.parkingLotName shouldBe parkingLot.name
        parkingRecord.parkedAt shouldNotBe null
        parkingRecord.leftAt shouldBe null
        
        // 주차장 상태 확인
        parkingLot.availableSpaces.value shouldBe 9
        parkingLot.occupiedSpaces.value shouldBe 1
    }
    
    "ParkingLot에서 차량을 출차할 수 있다" {
        // given
        val parkingLot = ParkingLot(
            name = ParkingLotName("출차 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(10)
        )
        val licensePlateNumber = LicensePlateNumber("456나5678")
        val parkingRecord = parkingLot.parkCar(licensePlateNumber)
        
        // when
        val leftRecord = parkingLot.leaveCar(licensePlateNumber)
        
        // then
        leftRecord.shouldBeInstanceOf<ParkingRecord>()
        leftRecord.licensePlateNumber shouldBe licensePlateNumber
        leftRecord.parkingLotName shouldBe parkingLot.name
        leftRecord.parkedAt shouldBe parkingRecord.parkedAt
        leftRecord.leftAt shouldNotBe null
        
        // 주차장 상태 확인
        parkingLot.availableSpaces.value shouldBe 10
        parkingLot.occupiedSpaces.value shouldBe 0
    }
    
    "주차 공간이 가득 찬 경우 예외가 발생한다" {
        // given
        val parkingLot = ParkingLot(
            name = ParkingLotName("만차 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(1)
        )
        val firstCar = LicensePlateNumber("111가1111")
        val secondCar = LicensePlateNumber("222나2222")
        
        parkingLot.parkCar(firstCar)
        
        // when & then
        try {
            parkingLot.parkCar(secondCar)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "주차장이 만차입니다"
        }
    }
    
    "주차되지 않은 차량을 출차하려 하면 예외가 발생한다" {
        // given
        val parkingLot = ParkingLot(
            name = ParkingLotName("출차 예외 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(10)
        )
        val licensePlateNumber = LicensePlateNumber("999가9999")
        
        // when & then
        try {
            parkingLot.leaveCar(licensePlateNumber)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "주차되지 않은 차량입니다"
        }
    }
    
    "이미 주차된 차량을 다시 주차하려 하면 예외가 발생한다" {
        // given
        val parkingLot = ParkingLot(
            name = ParkingLotName("중복 주차 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(10)
        )
        val licensePlateNumber = LicensePlateNumber("777다7777")
        
        parkingLot.parkCar(licensePlateNumber)
        
        // when & then
        try {
            parkingLot.parkCar(licensePlateNumber)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "이미 주차된 차량입니다"
        }
    }
    
    "ParkingLot의 상태를 조회할 수 있다" {
        // given
        val parkingLot = ParkingLot(
            name = ParkingLotName("상태 조회 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(20)
        )
        
        // 일부 차량 주차
        parkingLot.parkCar(LicensePlateNumber("111가1111"))
        parkingLot.parkCar(LicensePlateNumber("222나2222"))
        parkingLot.parkCar(LicensePlateNumber("333다3333"))
        
        // when
        val status = parkingLot.getStatus()
        
        // then
        status.shouldBeInstanceOf<ParkingLotStatus>()
        status.name shouldBe parkingLot.name
        status.totalSpaces shouldBe parkingLot.totalSpaces
        status.availableSpaces.value shouldBe 17
        status.occupiedSpaces.value shouldBe 3
        status.occupancyRate shouldBe 0.15 // 3/20 = 0.15
    }
    
    "ParkingLot에서 주차된 차량 목록을 조회할 수 있다" {
        // given
        val parkingLot = ParkingLot(
            name = ParkingLotName("차량 목록 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(10)
        )
        
        val car1 = LicensePlateNumber("111가1111")
        val car2 = LicensePlateNumber("222나2222")
        val car3 = LicensePlateNumber("333다3333")
        
        parkingLot.parkCar(car1)
        parkingLot.parkCar(car2)
        parkingLot.parkCar(car3)
        
        // when
        val parkedCars = parkingLot.getParkedCars()
        
        // then
        parkedCars.size shouldBe 3
        parkedCars.map { it.licensePlateNumber }.toSet() shouldBe setOf(car1, car2, car3)
        parkedCars.forEach { record ->
            record.leftAt shouldBe null
            record.parkedAt shouldNotBe null
        }
    }
    
    "ParkingLot은 도메인 불변성을 유지한다" {
        // given
        val parkingLot = ParkingLot(
            name = ParkingLotName("불변성 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(5)
        )
        
        // when & then - 총 주차 공간 수는 변경되지 않음
        parkingLot.totalSpaces.value shouldBe 5
        
        parkingLot.parkCar(LicensePlateNumber("111가1111"))
        parkingLot.totalSpaces.value shouldBe 5
        
        parkingLot.parkCar(LicensePlateNumber("222나2222"))
        parkingLot.totalSpaces.value shouldBe 5
        
        // 가용 공간과 점유 공간의 합은 항상 총 공간과 같음
        (parkingLot.availableSpaces.value + parkingLot.occupiedSpaces.value) shouldBe parkingLot.totalSpaces.value
    }
})
