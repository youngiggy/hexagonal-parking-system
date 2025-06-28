package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Instant

class ParkingRecordTest : StringSpec({
    "ParkingRecord가 생성된다" {
        // given
        val licensePlateNumber = LicensePlateNumber("123가1234")
        val parkingLotName = ParkingLotName("테스트 주차장")
        val parkedAt = Instant.now()
        
        // when
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = parkingLotName,
            parkedAt = parkedAt
        )
        
        // then
        parkingRecord.shouldBeInstanceOf<ParkingRecord>()
        parkingRecord.licensePlateNumber shouldBe licensePlateNumber
        parkingRecord.parkingLotName shouldBe parkingLotName
        parkingRecord.parkedAt shouldBe parkedAt
        parkingRecord.leftAt shouldBe null
        parkingRecord.isParked shouldBe true
    }
    
    "ParkingRecord에서 출차 처리를 할 수 있다" {
        // given
        val licensePlateNumber = LicensePlateNumber("456나5678")
        val parkingLotName = ParkingLotName("출차 테스트 주차장")
        val parkedAt = Instant.now()
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = parkingLotName,
            parkedAt = parkedAt
        )
        
        // when
        val leftRecord = parkingRecord.leave()
        
        // then
        leftRecord.shouldBeInstanceOf<ParkingRecord>()
        leftRecord.licensePlateNumber shouldBe licensePlateNumber
        leftRecord.parkingLotName shouldBe parkingLotName
        leftRecord.parkedAt shouldBe parkedAt
        leftRecord.leftAt shouldNotBe null
        leftRecord.isParked shouldBe false
    }
    
    "이미 출차한 차량을 다시 출차할 수 없다" {
        // given
        val licensePlateNumber = LicensePlateNumber("789다7890")
        val parkingLotName = ParkingLotName("중복 출차 테스트 주차장")
        val parkedAt = Instant.now()
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = parkingLotName,
            parkedAt = parkedAt
        )
        val leftRecord = parkingRecord.leave()
        
        // when & then
        try {
            leftRecord.leave()
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: IllegalStateException) {
            e.message shouldBe "이미 출차한 차량입니다"
        }
    }
    
    "ParkingRecord는 주차 시간을 계산할 수 있다" {
        // given
        val licensePlateNumber = LicensePlateNumber("111가1111")
        val parkingLotName = ParkingLotName("시간 계산 테스트 주차장")
        val parkedAt = Instant.now().minusSeconds(3600) // 1시간 전
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = parkingLotName,
            parkedAt = parkedAt
        )
        
        // when
        val parkingDuration = parkingRecord.getParkingDuration()
        
        // then
        parkingDuration.seconds shouldBe 3600L // 1시간 = 3600초
    }
})
