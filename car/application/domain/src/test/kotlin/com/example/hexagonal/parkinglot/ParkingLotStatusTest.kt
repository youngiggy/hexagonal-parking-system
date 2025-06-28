package com.example.hexagonal.parkinglot

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ParkingLotStatusTest : StringSpec({
    "ParkingLotStatus가 생성된다" {
        // given
        val name = ParkingLotName("상태 테스트 주차장")
        val totalSpaces = ParkingSpaceCount(100)
        val availableSpaces = ParkingSpaceCount(70)
        val occupiedSpaces = ParkingSpaceCount(30)
        val occupancyRate = 0.3
        
        // when
        val status = ParkingLotStatus(
            name = name,
            totalSpaces = totalSpaces,
            availableSpaces = availableSpaces,
            occupiedSpaces = occupiedSpaces,
            occupancyRate = occupancyRate
        )
        
        // then
        status.shouldBeInstanceOf<ParkingLotStatus>()
        status.name shouldBe name
        status.totalSpaces shouldBe totalSpaces
        status.availableSpaces shouldBe availableSpaces
        status.occupiedSpaces shouldBe occupiedSpaces
        status.occupancyRate shouldBe occupancyRate
    }
    
    "ParkingLotStatus는 만차 상태를 확인할 수 있다" {
        // given
        val fullStatus = ParkingLotStatus(
            name = ParkingLotName("만차 주차장"),
            totalSpaces = ParkingSpaceCount(50),
            availableSpaces = ParkingSpaceCount(0),
            occupiedSpaces = ParkingSpaceCount(50),
            occupancyRate = 1.0
        )
        
        val notFullStatus = ParkingLotStatus(
            name = ParkingLotName("여유 주차장"),
            totalSpaces = ParkingSpaceCount(50),
            availableSpaces = ParkingSpaceCount(10),
            occupiedSpaces = ParkingSpaceCount(40),
            occupancyRate = 0.8
        )
        
        // when & then
        fullStatus.isFull shouldBe true
        notFullStatus.isFull shouldBe false
    }
    
    "ParkingLotStatus는 빈 상태를 확인할 수 있다" {
        // given
        val emptyStatus = ParkingLotStatus(
            name = ParkingLotName("빈 주차장"),
            totalSpaces = ParkingSpaceCount(100),
            availableSpaces = ParkingSpaceCount(100),
            occupiedSpaces = ParkingSpaceCount(0),
            occupancyRate = 0.0
        )
        
        val notEmptyStatus = ParkingLotStatus(
            name = ParkingLotName("사용 중인 주차장"),
            totalSpaces = ParkingSpaceCount(100),
            availableSpaces = ParkingSpaceCount(95),
            occupiedSpaces = ParkingSpaceCount(5),
            occupancyRate = 0.05
        )
        
        // when & then
        emptyStatus.isEmpty shouldBe true
        notEmptyStatus.isEmpty shouldBe false
    }
    
    "ParkingLotStatus는 점유율을 백분율로 반환할 수 있다" {
        // given
        val status = ParkingLotStatus(
            name = ParkingLotName("백분율 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(200),
            availableSpaces = ParkingSpaceCount(125),
            occupiedSpaces = ParkingSpaceCount(75),
            occupancyRate = 0.375
        )
        
        // when
        val percentage = status.getOccupancyPercentage()
        
        // then
        percentage shouldBe 37 // 0.375 * 100 = 37.5 -> 37 (int)
    }
    
    "ParkingLotStatus는 잘못된 점유율을 허용하지 않는다" {
        // when & then - 음수 점유율
        try {
            ParkingLotStatus(
                name = ParkingLotName("잘못된 주차장"),
                totalSpaces = ParkingSpaceCount(100),
                availableSpaces = ParkingSpaceCount(100),
                occupiedSpaces = ParkingSpaceCount(0),
                occupancyRate = -0.1
            )
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "점유율은 0.0과 1.0 사이여야 합니다"
        }
        
        // when & then - 1.0 초과 점유율
        try {
            ParkingLotStatus(
                name = ParkingLotName("잘못된 주차장"),
                totalSpaces = ParkingSpaceCount(100),
                availableSpaces = ParkingSpaceCount(0),
                occupiedSpaces = ParkingSpaceCount(100),
                occupancyRate = 1.1
            )
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "점유율은 0.0과 1.0 사이여야 합니다"
        }
    }
    
    "ParkingLotStatus는 공간 수의 일관성을 검증한다" {
        // when & then - 전체 공간 수와 사용 가능/점유 공간 수의 합이 다른 경우
        try {
            ParkingLotStatus(
                name = ParkingLotName("일관성 오류 주차장"),
                totalSpaces = ParkingSpaceCount(100),
                availableSpaces = ParkingSpaceCount(60),
                occupiedSpaces = ParkingSpaceCount(50), // 60 + 50 = 110 != 100
                occupancyRate = 0.5
            )
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "전체 공간 수는 사용 가능 공간 수와 점유 공간 수의 합과 같아야 합니다"
        }
    }
})
