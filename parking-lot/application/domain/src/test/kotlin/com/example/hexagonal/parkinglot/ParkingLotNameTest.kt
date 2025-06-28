package com.example.hexagonal.parkinglot

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ParkingLotNameTest : StringSpec({
    "ParkingLotName이 생성된다" {
        // given
        val name = "메인 주차장"
        
        // when
        val parkingLotName = ParkingLotName(name)
        
        // then
        parkingLotName.shouldBeInstanceOf<ParkingLotName>()
        parkingLotName.value shouldBe name
    }
    
    "ParkingLotName은 빈 문자열을 허용하지 않는다" {
        // given
        val emptyName = ""
        
        // when & then
        try {
            ParkingLotName(emptyName)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "주차장 이름은 비어있을 수 없습니다"
        }
    }
    
    "ParkingLotName은 공백만 있는 문자열을 허용하지 않는다" {
        // given
        val blankName = "   "
        
        // when & then
        try {
            ParkingLotName(blankName)
            throw AssertionError("예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "주차장 이름은 비어있을 수 없습니다"
        }
    }
})
