package com.example.hexagonal.car

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.Instant
import java.util.UUID

class CarRestApiTest : StringSpec({
    "CarRestApi는 차량을 일괄 등록한다" {
        // given
        val mockQueryUseCase = mockk<CarQueryUseCase>()
        val mockCommandUseCase = mockk<CarCommandUseCase>()
        val api = CarRestApi(mockQueryUseCase, mockCommandUseCase)
        
        val requestDtos = listOf(
            CarRequestDto(licensePlateNumber = "123가1234"),
            CarRequestDto(licensePlateNumber = "456나5678")
        )
        
        val savedCars = requestDtos.map { dto ->
            CarEntity(
                licencePlateNumber = LicensePlateNumber(dto.licensePlateNumber),
                identity = CarKey(UUID.randomUUID()),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
        
        every { mockCommandUseCase.bulkCreateCar(any()) } returns savedCars
        
        // when
        val response = api.bulkCreateCars(requestDtos)
        
        // then
        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldNotBe null
        response.body!!.size shouldBe 2
        response.body!!.forEachIndexed { index, responseDto ->
            responseDto.licensePlateNumber shouldBe requestDtos[index].licensePlateNumber
        }
        verify(exactly = 1) { mockCommandUseCase.bulkCreateCar(any()) }
    }
    
    "CarRestApi는 빈 리스트로 일괄 등록을 처리한다" {
        // given
        val mockQueryUseCase = mockk<CarQueryUseCase>()
        val mockCommandUseCase = mockk<CarCommandUseCase>()
        val api = CarRestApi(mockQueryUseCase, mockCommandUseCase)
        
        val emptyRequestDtos = emptyList<CarRequestDto>()
        val emptyResult = emptyList<CarModel>()
        
        every { mockCommandUseCase.bulkCreateCar(any()) } returns emptyResult
        
        // when
        val response = api.bulkCreateCars(emptyRequestDtos)
        
        // then
        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldNotBe null
        response.body!!.size shouldBe 0
        verify(exactly = 1) { mockCommandUseCase.bulkCreateCar(any()) }
    }
    
    "CarRestApi는 의존성 주입을 통해 UseCase들을 받는다" {
        // given
        val queryUseCase = mockk<CarQueryUseCase>(relaxed = true)
        val commandUseCase = mockk<CarCommandUseCase>(relaxed = true)
        
        // when
        val api = CarRestApi(queryUseCase, commandUseCase)
        
        // then
        api.shouldBeInstanceOf<CarRestApi>()
        
        // 실제로 주입된 UseCase들이 사용되는지 확인
        val requestDto = CarRequestDto(licensePlateNumber = "111가1111")
        api.bulkCreateCars(listOf(requestDto))
        
        verify { commandUseCase.bulkCreateCar(any()) }
    }
    
    "CarRestApi는 DTO 변환을 올바르게 처리한다" {
        // given
        val mockQueryUseCase = mockk<CarQueryUseCase>()
        val mockCommandUseCase = mockk<CarCommandUseCase>()
        val api = CarRestApi(mockQueryUseCase, mockCommandUseCase)
        
        val licensePlateNumber = "222가2222"
        val requestDto = CarRequestDto(licensePlateNumber = licensePlateNumber)
        val carModel = CarEntity(
            licencePlateNumber = LicensePlateNumber(licensePlateNumber),
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        every { mockCommandUseCase.bulkCreateCar(any()) } returns listOf(carModel)
        
        // when
        val response = api.bulkCreateCars(listOf(requestDto))
        
        // then - DTO 변환이 올바르게 수행되어야 함
        response.body!!.first().licensePlateNumber shouldBe licensePlateNumber
        response.body!!.first().id shouldBe carModel.identity.value
        response.body!!.first().createdAt shouldBe carModel.createdAt
        response.body!!.first().updatedAt shouldBe carModel.updatedAt
    }
    
    "CarRestApi는 헥사고날 아키텍처 원칙을 준수한다" {
        // given
        val mockQueryUseCase = mockk<CarQueryUseCase>(relaxed = true)
        val mockCommandUseCase = mockk<CarCommandUseCase>(relaxed = true)
        
        // when
        val api = CarRestApi(mockQueryUseCase, mockCommandUseCase)
        
        // then - Primary Adapter로서 인바운드 포트 사용
        api.shouldBeInstanceOf<CarRestApi>()
        
        // 도메인 로직은 UseCase에 위임
        val licensePlateNumber = "333가3333"
        val requestDto = CarRequestDto(licensePlateNumber = licensePlateNumber)
        
        api.bulkCreateCars(listOf(requestDto))
        
        verify { mockCommandUseCase.bulkCreateCar(any()) }
    }
    
    "CarRestApi는 번호판으로 차량을 조회한다" {
        // given
        val mockQueryUseCase = mockk<CarQueryUseCase>()
        val mockCommandUseCase = mockk<CarCommandUseCase>()
        val api = CarRestApi(mockQueryUseCase, mockCommandUseCase)
        
        val licensePlateNumber = "789다7890"
        val carModel = CarEntity(
            licencePlateNumber = LicensePlateNumber(licensePlateNumber),
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        every { mockQueryUseCase.getByLicensePlateNumber(any()) } returns carModel
        
        // when
        val response = api.getCarByLicensePlateNumber(licensePlateNumber)
        
        // then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!.licensePlateNumber shouldBe licensePlateNumber
        response.body!!.id shouldBe carModel.identity.value
        verify(exactly = 1) { mockQueryUseCase.getByLicensePlateNumber(any()) }
    }
    
    "CarRestApi는 존재하지 않는 차량 조회시 404를 반환한다" {
        // given
        val mockQueryUseCase = mockk<CarQueryUseCase>()
        val mockCommandUseCase = mockk<CarCommandUseCase>()
        val api = CarRestApi(mockQueryUseCase, mockCommandUseCase)
        
        val licensePlateNumber = "999가9999"
        
        every { mockQueryUseCase.getByLicensePlateNumber(any()) } throws CarNotFoundException("등록되지 않은 자동차입니다")
        
        // when
        val response = api.getCarByLicensePlateNumber(licensePlateNumber)
        
        // then
        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body shouldBe null
        verify(exactly = 1) { mockQueryUseCase.getByLicensePlateNumber(any()) }
    }
    
    "CarRestApi는 잘못된 번호판 형식에 대해 400을 반환한다" {
        // given
        val mockQueryUseCase = mockk<CarQueryUseCase>()
        val mockCommandUseCase = mockk<CarCommandUseCase>()
        val api = CarRestApi(mockQueryUseCase, mockCommandUseCase)
        
        val invalidLicensePlateNumber = "잘못된형식"
        
        // when
        val response = api.getCarByLicensePlateNumber(invalidLicensePlateNumber)
        
        // then
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body shouldBe null
    }
    
    "CarRestApi는 올바른 HTTP 상태 코드를 반환한다" {
        // given
        val mockQueryUseCase = mockk<CarQueryUseCase>()
        val mockCommandUseCase = mockk<CarCommandUseCase>()
        val api = CarRestApi(mockQueryUseCase, mockCommandUseCase)
        
        val requestDto = CarRequestDto(licensePlateNumber = "111가1111")
        val savedCar = CarEntity(
            licencePlateNumber = LicensePlateNumber(requestDto.licensePlateNumber),
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        every { mockCommandUseCase.bulkCreateCar(any()) } returns listOf(savedCar)
        every { mockQueryUseCase.getByLicensePlateNumber(any()) } returns savedCar
        
        // when & then
        val createResponse = api.bulkCreateCars(listOf(requestDto))
        createResponse.statusCode shouldBe HttpStatus.CREATED
        
        val getResponse = api.getCarByLicensePlateNumber("111가1111")
        getResponse.statusCode shouldBe HttpStatus.OK
    }
})
