package com.example.hexagonal.car

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class CarNotFoundExceptionTest : StringSpec({
    "CarNotFoundException이 메시지와 함께 생성된다" {
        // given
        val message = "등록되지 않은 자동차입니다"
        
        // when
        val exception = CarNotFoundException(message)
        
        // then
        exception.message shouldBe message
        exception.shouldBeInstanceOf<RuntimeException>()
        exception.cause shouldBe null
    }
    
    "CarNotFoundException은 RuntimeException을 상속한다" {
        // given
        val message = "차량을 찾을 수 없습니다"
        
        // when
        val exception = CarNotFoundException(message)
        
        // then
        val runtimeException: RuntimeException = exception // 타입 체크
        runtimeException.message shouldBe message
    }
    
    "CarNotFoundException은 Throwable을 상속한다" {
        // given
        val message = "차량 조회 실패"
        
        // when
        val exception = CarNotFoundException(message)
        
        // then
        val throwable: Throwable = exception // 타입 체크
        throwable.message shouldBe message
    }
    
    "빈 메시지로 CarNotFoundException이 생성된다" {
        // given
        val message = ""
        
        // when
        val exception = CarNotFoundException(message)
        
        // then
        exception.message shouldBe message
    }
    
    "null 메시지로 CarNotFoundException이 생성된다" {
        // given
        val message: String? = null
        
        // when
        val exception = CarNotFoundException(message)
        
        // then
        exception.message shouldBe message
    }
    
    "CarNotFoundException이 메시지와 원인 예외와 함께 생성된다" {
        // given
        val message = "데이터베이스 연결 실패로 인한 차량 조회 실패"
        val cause = RuntimeException("Database connection failed")
        
        // when
        val exception = CarNotFoundException(message, cause)
        
        // then
        exception.message shouldBe message
        exception.cause shouldBe cause
    }
    
    "CarNotFoundException이 원인 예외와 함께 생성된다" {
        // given
        val cause = IllegalStateException("Invalid state")
        
        // when
        val exception = CarNotFoundException(cause)
        
        // then
        exception.cause shouldBe cause
        exception.message shouldBe cause.toString()
    }
    
    "CarNotFoundException은 스택 트레이스를 가진다" {
        // given
        val message = "스택 트레이스 테스트"
        
        // when
        val exception = CarNotFoundException(message)
        
        // then
        exception.stackTrace.shouldBeInstanceOf<Array<StackTraceElement>>()
        exception.stackTrace.size shouldNotBe 0
    }
    
    "동일한 메시지를 가진 CarNotFoundException은 메시지가 같다" {
        // given
        val message = "동일한 메시지"
        val exception1 = CarNotFoundException(message)
        val exception2 = CarNotFoundException(message)
        
        // when & then
        exception1.message shouldBe exception2.message
    }
    
    "다른 메시지를 가진 CarNotFoundException은 메시지가 다르다" {
        // given
        val message1 = "첫 번째 메시지"
        val message2 = "두 번째 메시지"
        val exception1 = CarNotFoundException(message1)
        val exception2 = CarNotFoundException(message2)
        
        // when & then
        exception1.message shouldBe message1
        exception2.message shouldBe message2
    }
    
    "CarNotFoundException 체이닝이 올바르게 동작한다" {
        // given
        val rootCause = IllegalArgumentException("Root cause")
        val intermediateCause = RuntimeException("Intermediate cause", rootCause)
        val finalException = CarNotFoundException("Final exception", intermediateCause)
        
        // when & then
        finalException.cause shouldBe intermediateCause
        finalException.cause?.cause shouldBe rootCause
    }
})
