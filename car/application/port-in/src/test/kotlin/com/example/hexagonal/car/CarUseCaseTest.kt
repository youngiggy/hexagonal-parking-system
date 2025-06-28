package com.example.hexagonal.car

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class CarUseCaseTest : StringSpec({
    "CarQueryUseCase 인터페이스가 정의되고 동작한다" {
        // given
        val mockUseCase = mockk<CarQueryUseCase>()
        val licensePlateNumber = LicensePlateNumber("123가1234")
        val expectedCar = mockk<CarModel>()
        
        every { mockUseCase.getByLicensePlateNumber(licensePlateNumber) } returns expectedCar
        
        // when
        val result = mockUseCase.getByLicensePlateNumber(licensePlateNumber)
        
        // then
        result shouldBe expectedCar
        result.shouldBeInstanceOf<CarModel>()
        verify { mockUseCase.getByLicensePlateNumber(licensePlateNumber) }
    }
    
    "CarCommandUseCase 인터페이스가 정의되고 동작한다" {
        // given
        val mockUseCase = mockk<CarCommandUseCase>()
        val carData = listOf(CarData(LicensePlateNumber("123가1234")))
        val expectedCars = listOf(mockk<CarModel>())
        
        every { mockUseCase.bulkCreateCar(carData) } returns expectedCars
        
        // when
        val result = mockUseCase.bulkCreateCar(carData)
        
        // then
        result shouldBe expectedCars
        result.shouldBeInstanceOf<Collection<CarModel>>()
        verify { mockUseCase.bulkCreateCar(carData) }
    }
    
    "CarQueryUseCase는 LicensePlateNumber를 받아 CarModel을 반환한다" {
        // given
        val mockUseCase = mockk<CarQueryUseCase>(relaxed = true)
        val licensePlateNumber = LicensePlateNumber("456나7890")
        
        // when
        val result = mockUseCase.getByLicensePlateNumber(licensePlateNumber)
        
        // then
        result.shouldBeInstanceOf<CarModel>()
        verify { mockUseCase.getByLicensePlateNumber(licensePlateNumber) }
    }
    
    "CarCommandUseCase는 CarProperties 컬렉션을 받아 CarModel 컬렉션을 반환한다" {
        // given
        val mockUseCase = mockk<CarCommandUseCase>()
        val carProperties = listOf(
            CarData(LicensePlateNumber("111가1111")),
            CarData(LicensePlateNumber("222나2222"))
        )
        val expectedCars = listOf(mockk<CarModel>(), mockk<CarModel>())
        
        every { mockUseCase.bulkCreateCar(carProperties) } returns expectedCars
        
        // when
        val result = mockUseCase.bulkCreateCar(carProperties)
        
        // then
        result shouldBe expectedCars
        result.size shouldBe 2
        result.shouldBeInstanceOf<Collection<CarModel>>()
        verify { mockUseCase.bulkCreateCar(carProperties) }
    }
    
    "빈 컬렉션으로 bulkCreateCar를 호출할 수 있다" {
        // given
        val mockUseCase = mockk<CarCommandUseCase>()
        val emptyCarData = emptyList<CarProperties>()
        val emptyResult = emptyList<CarModel>()
        
        every { mockUseCase.bulkCreateCar(emptyCarData) } returns emptyResult
        
        // when
        val result = mockUseCase.bulkCreateCar(emptyCarData)
        
        // then
        result shouldBe emptyResult
        result.size shouldBe 0
        result.shouldBeInstanceOf<Collection<CarModel>>()
        verify { mockUseCase.bulkCreateCar(emptyCarData) }
    }
    
    "CarQueryUseCase와 CarCommandUseCase는 서로 다른 인터페이스다" {
        // given
        val queryUseCase = mockk<CarQueryUseCase>(relaxed = true)
        val commandUseCase = mockk<CarCommandUseCase>(relaxed = true)
        
        // when & then
        queryUseCase.shouldBeInstanceOf<CarQueryUseCase>()
        commandUseCase.shouldBeInstanceOf<CarCommandUseCase>()
        
        // 서로 다른 타입임을 확인
        (queryUseCase is CarCommandUseCase) shouldBe false
        (commandUseCase is CarQueryUseCase) shouldBe false
    }
})
