package com.example.hexagonal.car

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import java.time.Instant
import java.util.UUID

class CarServiceTest : StringSpec({
    "CarService는 인바운드 포트들을 구현한다" {
        // given
        val mockSavePort = mockk<CarSavePort>(relaxed = true)
        val mockLoadPort = mockk<CarLoadPort>(relaxed = true)
        
        // when
        val service = CarService(mockSavePort, mockLoadPort)
        
        // then
        service.shouldBeInstanceOf<CarQueryUseCase>()
        service.shouldBeInstanceOf<CarCommandUseCase>()
        service.shouldBeInstanceOf<CarService>()
    }
    
    "차량을 일괄 등록한다" {
        // given
        val mockSavePort = mockk<CarSavePort>()
        val mockLoadPort = mockk<CarLoadPort>()
        val service = CarService(mockSavePort, mockLoadPort)
        
        val carData = listOf(
            CarData(LicensePlateNumber("123가1234")),
            CarData(LicensePlateNumber("456나5678"))
        )
        val expectedCars = carData.map { data ->
            CarEntity(
                licencePlateNumber = data.licencePlateNumber,
                identity = CarKey(UUID.randomUUID()),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
        
        every { mockSavePort.saveAll(carData) } returns expectedCars
        
        // when
        val result = service.bulkCreateCar(carData)
        
        // then
        result shouldBe expectedCars
        result.size shouldBe 2
        result.forEachIndexed { index, carModel ->
            carModel.licencePlateNumber shouldBe carData[index].licencePlateNumber
            carModel.shouldBeInstanceOf<CarModel>()
        }
        verify(exactly = 1) { mockSavePort.saveAll(carData) }
    }
    
    "번호판으로 차량을 조회한다" {
        // given
        val mockSavePort = mockk<CarSavePort>()
        val mockLoadPort = mockk<CarLoadPort>()
        val service = CarService(mockSavePort, mockLoadPort)
        
        val licensePlateNumber = LicensePlateNumber("789다7890")
        val expectedCar = CarEntity(
            licencePlateNumber = licensePlateNumber,
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        every { mockLoadPort.findByLicensePlateNumberOrNull(licensePlateNumber) } returns expectedCar
        
        // when
        val result = service.getByLicensePlateNumber(licensePlateNumber)
        
        // then
        result shouldBe expectedCar
        result.licencePlateNumber shouldBe licensePlateNumber
        result.shouldBeInstanceOf<CarModel>()
        verify(exactly = 1) { mockLoadPort.findByLicensePlateNumberOrNull(licensePlateNumber) }
    }
    
    "등록되지 않은 차량 조회시 CarNotFoundException이 발생한다" {
        // given
        val mockSavePort = mockk<CarSavePort>()
        val mockLoadPort = mockk<CarLoadPort>()
        val service = CarService(mockSavePort, mockLoadPort)
        
        val licensePlateNumber = LicensePlateNumber("999라9999")
        
        every { mockLoadPort.findByLicensePlateNumberOrNull(licensePlateNumber) } returns null
        
        // when & then
        val exception = shouldThrow<CarNotFoundException> {
            service.getByLicensePlateNumber(licensePlateNumber)
        }
        
        exception.message shouldBe "등록되지 않은 자동차입니다"
        exception.shouldBeInstanceOf<RuntimeException>()
        verify(exactly = 1) { mockLoadPort.findByLicensePlateNumberOrNull(licensePlateNumber) }
    }
    
    "빈 컬렉션으로 일괄 등록을 호출할 수 있다" {
        // given
        val mockSavePort = mockk<CarSavePort>()
        val mockLoadPort = mockk<CarLoadPort>()
        val service = CarService(mockSavePort, mockLoadPort)
        
        val emptyCarData = emptyList<CarProperties>()
        val emptyResult = emptyList<CarModel>()
        
        every { mockSavePort.saveAll(emptyCarData) } returns emptyResult
        
        // when
        val result = service.bulkCreateCar(emptyCarData)
        
        // then
        result shouldBe emptyResult
        result.size shouldBe 0
        result.shouldBeInstanceOf<Collection<CarModel>>()
        verify(exactly = 1) { mockSavePort.saveAll(emptyCarData) }
    }
    
    "CarService는 의존성 주입을 통해 포트들을 받는다" {
        // given
        val savePort = mockk<CarSavePort>(relaxed = true)
        val loadPort = mockk<CarLoadPort>(relaxed = true)
        
        // when
        val service = CarService(savePort, loadPort)
        
        // then - 생성자 주입이 정상적으로 동작함을 확인
        service.shouldBeInstanceOf<CarService>()
        
        // 실제로 주입된 포트들이 사용되는지 확인
        val licensePlateNumber = LicensePlateNumber("111가1111")
        val carData = listOf(CarData(licensePlateNumber))
        
        service.bulkCreateCar(carData)
        verify { savePort.saveAll(carData) }
    }
    
    "여러 차량을 동시에 등록할 수 있다" {
        // given
        val mockSavePort = mockk<CarSavePort>()
        val mockLoadPort = mockk<CarLoadPort>()
        val service = CarService(mockSavePort, mockLoadPort)
        
        val multipleCarData = (1..5).map { 
            CarData(LicensePlateNumber("${it}가${1000 + it}"))
        }
        val savedCars = multipleCarData.map { data ->
            CarEntity(
                licencePlateNumber = data.licencePlateNumber,
                identity = CarKey(UUID.randomUUID()),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
        
        every { mockSavePort.saveAll(multipleCarData) } returns savedCars
        
        // when
        val result = service.bulkCreateCar(multipleCarData)
        
        // then
        result.size shouldBe 5
        result.forEachIndexed { index, carModel ->
            carModel.licencePlateNumber shouldBe multipleCarData[index].licencePlateNumber
            carModel.shouldBeInstanceOf<CarModel>()
        }
        verify(exactly = 1) { mockSavePort.saveAll(multipleCarData) }
    }
    
    "CarService는 헥사고날 아키텍처 원칙을 준수한다" {
        // given
        val mockSavePort = mockk<CarSavePort>(relaxed = true)
        val mockLoadPort = mockk<CarLoadPort>(relaxed = true)
        
        // when
        val service = CarService(mockSavePort, mockLoadPort)
        
        // then - 인바운드 포트 구현
        service.shouldBeInstanceOf<CarQueryUseCase>()
        service.shouldBeInstanceOf<CarCommandUseCase>()
        
        // 아웃바운드 포트 사용 (의존성 역전)
        val licensePlateNumber = LicensePlateNumber("456나7890")
        val carData = listOf(CarData(licensePlateNumber))
        
        service.bulkCreateCar(carData)
        verify { mockSavePort.saveAll(carData) }
    }
    
    "비즈니스 로직 처리 순서가 올바르다" {
        // given
        val mockSavePort = mockk<CarSavePort>()
        val mockLoadPort = mockk<CarLoadPort>()
        val service = CarService(mockSavePort, mockLoadPort)
        
        val licensePlateNumber = LicensePlateNumber("111가1111")
        val carData = listOf(CarData(licensePlateNumber))
        val savedCars = listOf(mockk<CarModel>())
        
        every { mockSavePort.saveAll(carData) } returns savedCars
        every { mockLoadPort.findByLicensePlateNumberOrNull(licensePlateNumber) } returns savedCars.first()
        
        // when
        service.bulkCreateCar(carData)
        service.getByLicensePlateNumber(licensePlateNumber)
        
        // then - 호출 순서 검증
        verifyOrder {
            mockSavePort.saveAll(carData)
            mockLoadPort.findByLicensePlateNumberOrNull(licensePlateNumber)
        }
    }
    
    "예외 메시지가 명확하다" {
        // given
        val mockSavePort = mockk<CarSavePort>()
        val mockLoadPort = mockk<CarLoadPort>()
        val service = CarService(mockSavePort, mockLoadPort)
        
        val licensePlateNumber = LicensePlateNumber("999가9999")
        
        every { mockLoadPort.findByLicensePlateNumberOrNull(licensePlateNumber) } returns null
        
        // when & then
        val exception = shouldThrow<CarNotFoundException> {
            service.getByLicensePlateNumber(licensePlateNumber)
        }
        
        exception.message shouldContain "등록되지 않은"
        exception.message shouldContain "자동차"
    }
})
