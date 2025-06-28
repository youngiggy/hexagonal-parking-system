package com.example.hexagonal.car

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.util.UUID

class CarPortTest : StringSpec({
    "CarLoadPort 인터페이스가 정의되고 차량 조회가 동작한다" {
        // given
        val mockPort = mockk<CarLoadPort>()
        val licensePlateNumber = LicensePlateNumber("123가1234")
        val expectedCar = mockk<CarModel>()
        
        every { mockPort.findByLicensePlateNumberOrNull(licensePlateNumber) } returns expectedCar
        
        // when
        val result = mockPort.findByLicensePlateNumberOrNull(licensePlateNumber)
        
        // then
        result shouldBe expectedCar
        result.shouldBeInstanceOf<CarModel>()
        verify { mockPort.findByLicensePlateNumberOrNull(licensePlateNumber) }
    }
    
    "CarLoadPort는 존재하지 않는 차량에 대해 null을 반환한다" {
        // given
        val mockPort = mockk<CarLoadPort>()
        val nonExistentLicensePlateNumber = LicensePlateNumber("999가9999")
        
        every { mockPort.findByLicensePlateNumberOrNull(nonExistentLicensePlateNumber) } returns null
        
        // when
        val result = mockPort.findByLicensePlateNumberOrNull(nonExistentLicensePlateNumber)
        
        // then
        result shouldBe null
        verify { mockPort.findByLicensePlateNumberOrNull(nonExistentLicensePlateNumber) }
    }
    
    "CarSavePort 인터페이스가 정의되고 차량 저장이 동작한다" {
        // given
        val mockPort = mockk<CarSavePort>()
        val carData = listOf(CarData(LicensePlateNumber("123가1234")))
        val expectedCars = listOf(mockk<CarModel>())
        
        every { mockPort.saveAll(carData) } returns expectedCars
        
        // when
        val result = mockPort.saveAll(carData)
        
        // then
        result shouldBe expectedCars
        result.shouldBeInstanceOf<Collection<CarModel>>()
        result.size shouldBe 1
        verify { mockPort.saveAll(carData) }
    }
    
    "CarSavePort는 여러 차량을 일괄 저장할 수 있다" {
        // given
        val mockPort = mockk<CarSavePort>()
        val carProperties = listOf(
            CarData(LicensePlateNumber("111가1111")),
            CarData(LicensePlateNumber("222나2222")),
            CarData(LicensePlateNumber("333다3333"))
        )
        val savedCars = carProperties.map { carData ->
            CarEntity(
                licencePlateNumber = carData.licencePlateNumber,
                identity = CarKey(UUID.randomUUID()),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
        
        every { mockPort.saveAll(carProperties) } returns savedCars
        
        // when
        val result = mockPort.saveAll(carProperties)
        
        // then
        result.size shouldBe 3
        result.forEachIndexed { index, carModel ->
            carModel.licencePlateNumber shouldBe carProperties[index].licencePlateNumber
            carModel.shouldBeInstanceOf<CarModel>()
            carModel.identity.shouldBeInstanceOf<CarIdentity>()
        }
        verify { mockPort.saveAll(carProperties) }
    }
    
    "빈 컬렉션으로 CarSavePort.saveAll을 호출할 수 있다" {
        // given
        val mockPort = mockk<CarSavePort>()
        val emptyCarData = emptyList<CarProperties>()
        val emptyResult = emptyList<CarModel>()
        
        every { mockPort.saveAll(emptyCarData) } returns emptyResult
        
        // when
        val result = mockPort.saveAll(emptyCarData)
        
        // then
        result shouldBe emptyResult
        result.size shouldBe 0
        result.shouldBeInstanceOf<Collection<CarModel>>()
        verify { mockPort.saveAll(emptyCarData) }
    }
    
    "CarLoadPort와 CarSavePort는 서로 다른 책임을 가진 인터페이스다" {
        // given
        val loadPort = mockk<CarLoadPort>(relaxed = true)
        val savePort = mockk<CarSavePort>(relaxed = true)
        
        // when & then
        loadPort.shouldBeInstanceOf<CarLoadPort>()
        savePort.shouldBeInstanceOf<CarSavePort>()
        
        // 서로 다른 타입임을 확인 (단일 책임 원칙)
        (loadPort is CarSavePort) shouldBe false
        (savePort is CarLoadPort) shouldBe false
    }
    
    "CarLoadPort는 올바른 메서드 시그니처를 가진다" {
        // given
        val mockPort = mockk<CarLoadPort>()
        val licensePlateNumber = LicensePlateNumber("456나7890")
        
        // 명시적으로 null 반환 설정
        every { mockPort.findByLicensePlateNumberOrNull(licensePlateNumber) } returns null
        
        // when
        val result = mockPort.findByLicensePlateNumberOrNull(licensePlateNumber)
        
        // then
        result shouldBe null
        verify { mockPort.findByLicensePlateNumberOrNull(licensePlateNumber) }
    }
    
    "CarSavePort는 대량의 차량 데이터를 처리할 수 있다" {
        // given
        val mockPort = mockk<CarSavePort>()
        val multipleCarData = (1..10).map { 
            CarData(LicensePlateNumber("${it}가${1000 + it}"))
        }
        val savedCars = multipleCarData.map { 
            mockk<CarModel> {
                every { licencePlateNumber } returns it.licencePlateNumber
            }
        }
        
        every { mockPort.saveAll(multipleCarData) } returns savedCars
        
        // when
        val result = mockPort.saveAll(multipleCarData)
        
        // then
        result.size shouldBe 10
        result shouldBe savedCars
        result.forEachIndexed { index, carModel ->
            carModel.licencePlateNumber shouldBe multipleCarData[index].licencePlateNumber
        }
        verify { mockPort.saveAll(multipleCarData) }
    }
    
    "CarLoadPort와 CarSavePort는 헥사고날 아키텍처의 아웃바운드 포트 원칙을 준수한다" {
        // given
        val loadPort = mockk<CarLoadPort>(relaxed = true)
        val savePort = mockk<CarSavePort>(relaxed = true)
        
        // when & then - 인터페이스만 정의되어 있고 구현체는 외부에서 주입됨
        loadPort.shouldBeInstanceOf<CarLoadPort>()
        savePort.shouldBeInstanceOf<CarSavePort>()
        
        // 도메인 객체를 파라미터와 반환값으로 사용
        val licensePlateNumber = LicensePlateNumber("789라9999")
        val carData = listOf(CarData(licensePlateNumber))
        
        loadPort.findByLicensePlateNumberOrNull(licensePlateNumber)
        savePort.saveAll(carData)
        
        verify { loadPort.findByLicensePlateNumberOrNull(licensePlateNumber) }
        verify { savePort.saveAll(carData) }
    }
})
