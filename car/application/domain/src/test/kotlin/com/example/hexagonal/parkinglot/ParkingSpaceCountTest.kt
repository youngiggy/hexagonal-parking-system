package com.example.hexagonal.parkinglot

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ParkingSpaceCountTest : StringSpec({
    "ParkingSpaceCount가 생성된다" {
        // given
        val count = 100
        
        // when
        val parkingSpaceCount = ParkingSpaceCount(count)
        
        // then
        parkingSpaceCount.shouldBeInstanceOf<ParkingSpaceCount>()
        parkingSpaceCount.value shouldBe count
    }
    
    "ParkingSpaceCount는 음수를 허용하지 않는다" {
        // given
        val negativeCount = -1
        
        // when & then
        try {
            ParkingSpaceCount(negativeCount)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "주차 공간 수는 0 이상이어야 합니다"
        }
    }
    
    "ParkingSpaceCount는 0을 허용한다" {
        // given
        val zeroCount = 0
        
        // when
        val parkingSpaceCount = ParkingSpaceCount(zeroCount)
        
        // then
        parkingSpaceCount.value shouldBe 0
    }
    
    "ParkingSpaceCount는 산술 연산을 지원한다" {
        // given
        val count1 = ParkingSpaceCount(10)
        val count2 = ParkingSpaceCount(5)
        
        // when & then
        (count1 + count2).value shouldBe 15
        (count1 - count2).value shouldBe 5
        count1.isGreaterThan(count2) shouldBe true
        count2.isLessThan(count1) shouldBe true
        count1.equals(ParkingSpaceCount(10)) shouldBe true
    }
})
