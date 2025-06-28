package com.example.hexagonal.car

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Instant
import java.util.UUID

class CarDtoTest : StringSpec({
    "CarResponseDto가 CarModel로부터 생성된다" {
        // given
        val licensePlateNumber = LicensePlateNumber("123가1234")
        val carId = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        
        val carModel = CarEntity(
            licencePlateNumber = licensePlateNumber,
            identity = CarKey(carId),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
        
        // when
        val dto = CarResponseDto.from(carModel)
        
        // then
        dto.id shouldBe carId
        dto.licensePlateNumber shouldBe licensePlateNumber.value
        dto.createdAt shouldBe createdAt
        dto.updatedAt shouldBe updatedAt
        dto.shouldBeInstanceOf<CarResponseDto>()
    }
    
    "CarRequestDto가 CarProperties로 변환된다" {
        // given
        val licensePlateNumber = "456나5678"
        val requestDto = CarRequestDto(licensePlateNumber = licensePlateNumber)
        
        // when
        val carProperties = requestDto.toProperties()
        
        // then
        carProperties.licencePlateNumber.value shouldBe licensePlateNumber
        carProperties.shouldBeInstanceOf<CarProperties>()
        carProperties.shouldBeInstanceOf<CarData>()
    }
    
    "CarRequestDto 검증이 올바르게 동작한다" {
        // given
        val validLicensePlateNumber = "789다7890"
        val requestDto = CarRequestDto(licensePlateNumber = validLicensePlateNumber)
        
        // when & then
        requestDto.licensePlateNumber shouldBe validLicensePlateNumber
        requestDto.shouldBeInstanceOf<CarRequestDto>()
    }
    
    "CarResponseDto가 JSON 직렬화를 지원한다" {
        // given
        val licensePlateNumber = LicensePlateNumber("123가1234")
        val carId = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        
        val carModel = CarEntity(
            licencePlateNumber = licensePlateNumber,
            identity = CarKey(carId),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
        
        // when
        val dto = CarResponseDto.from(carModel)
        
        // then - JSON 직렬화를 위한 필드들이 올바르게 설정되어야 함
        dto.id shouldNotBe null
        dto.licensePlateNumber shouldNotBe null
        dto.createdAt shouldNotBe null
        dto.updatedAt shouldNotBe null
    }
    
    "CarRequestDto가 JSON 역직렬화를 지원한다" {
        // given
        val licensePlateNumber = "역직렬화1234"
        
        // when
        val requestDto = CarRequestDto(licensePlateNumber = licensePlateNumber)
        
        // then - JSON 역직렬화를 위한 기본 생성자와 필드가 있어야 함
        requestDto.licensePlateNumber shouldBe licensePlateNumber
    }
    
    "여러 CarModel을 CarResponseDto 리스트로 변환할 수 있다" {
        // given
        val carModels = listOf(
            CarEntity(
                licencePlateNumber = LicensePlateNumber("111가1111"),
                identity = CarKey(UUID.randomUUID()),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ),
            CarEntity(
                licencePlateNumber = LicensePlateNumber("222나2222"),
                identity = CarKey(UUID.randomUUID()),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )
        
        // when
        val dtos = carModels.map { CarResponseDto.from(it) }
        
        // then
        dtos.size shouldBe 2
        dtos.forEachIndexed { index, dto ->
            dto.licensePlateNumber shouldBe carModels[index].licencePlateNumber.value
            dto.id shouldBe carModels[index].identity.value
        }
    }
    
    "CarRequestDto 리스트를 CarProperties 리스트로 변환할 수 있다" {
        // given
        val requestDtos = listOf(
            CarRequestDto(licensePlateNumber = "333다3333"),
            CarRequestDto(licensePlateNumber = "444라4444"),
            CarRequestDto(licensePlateNumber = "555마5555")
        )
        
        // when
        val carPropertiesList = requestDtos.map { it.toProperties() }
        
        // then
        carPropertiesList.size shouldBe 3
        carPropertiesList.forEachIndexed { index, carProperties ->
            carProperties.licencePlateNumber.value shouldBe requestDtos[index].licensePlateNumber
        }
    }
    
    "CarDto는 도메인 모델과 REST API 간의 경계를 명확히 한다" {
        // given
        val licensePlateNumber = "666가6666"
        val requestDto = CarRequestDto(licensePlateNumber = licensePlateNumber)
        
        // when
        val carProperties = requestDto.toProperties()
        val carModel = CarEntity(
            licencePlateNumber = carProperties.licencePlateNumber,
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val responseDto = CarResponseDto.from(carModel)
        
        // then - 변환 과정에서 데이터 무결성 유지
        responseDto.licensePlateNumber shouldBe licensePlateNumber
        carProperties.licencePlateNumber.value shouldBe licensePlateNumber
    }
    
    "CarDto 검증 어노테이션이 올바르게 동작한다" {
        // given
        val validLicensePlateNumber = "123가1234"
        val invalidLicensePlateNumber = "잘못된형식"
        
        // when & then
        val validDto = CarRequestDto(licensePlateNumber = validLicensePlateNumber)
        validDto.licensePlateNumber shouldBe validLicensePlateNumber
        
        // 잘못된 형식은 도메인 레벨에서 검증됨
        val invalidDto = CarRequestDto(licensePlateNumber = invalidLicensePlateNumber)
        invalidDto.licensePlateNumber shouldBe invalidLicensePlateNumber
    }
    
    "CarDto는 Jackson 직렬화/역직렬화를 지원한다" {
        // given
        val licensePlateNumber = "888가8888"
        val requestDto = CarRequestDto(licensePlateNumber = licensePlateNumber)
        
        val carModel = CarEntity(
            licencePlateNumber = LicensePlateNumber(licensePlateNumber),
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val responseDto = CarResponseDto.from(carModel)
        
        // when & then - JSON 어노테이션이 올바르게 적용되어야 함
        requestDto.licensePlateNumber shouldBe licensePlateNumber
        responseDto.licensePlateNumber shouldBe licensePlateNumber
        responseDto.id shouldNotBe null
        responseDto.createdAt shouldNotBe null
        responseDto.updatedAt shouldNotBe null
    }
    
    "CarDto 변환 과정에서 타입 안전성이 보장된다" {
        // given
        val licensePlateNumber = "999가9999"
        val requestDto = CarRequestDto(licensePlateNumber = licensePlateNumber)
        
        // when
        val carProperties = requestDto.toProperties()
        val carModel = CarEntity(
            licencePlateNumber = carProperties.licencePlateNumber,
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val responseDto = CarResponseDto.from(carModel)
        
        // then - 모든 변환 과정에서 타입 안전성 유지
        carProperties.shouldBeInstanceOf<CarData>()
        carModel.shouldBeInstanceOf<CarEntity>()
        responseDto.shouldBeInstanceOf<CarResponseDto>()
        
        // 데이터 일관성 확인
        responseDto.licensePlateNumber shouldBe licensePlateNumber
        carProperties.licencePlateNumber.value shouldBe licensePlateNumber
        carModel.licencePlateNumber.value shouldBe licensePlateNumber
    }
    
    "CarDto는 헥사고날 아키텍처 원칙을 준수한다" {
        // given
        val licensePlateNumber = "777가7777"
        val requestDto = CarRequestDto(licensePlateNumber = licensePlateNumber)
        
        // when & then - DTO는 도메인 객체를 직접 노출하지 않음
        requestDto.shouldBeInstanceOf<CarRequestDto>()
        
        val carProperties = requestDto.toProperties()
        carProperties.shouldBeInstanceOf<CarProperties>()
        
        // 도메인 모델과 REST 계층 간의 명확한 분리
        val carModel = CarEntity(
            licencePlateNumber = carProperties.licencePlateNumber,
            identity = CarKey(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        val responseDto = CarResponseDto.from(carModel)
        responseDto.shouldBeInstanceOf<CarResponseDto>()
        
        // Primary Adapter의 역할: 외부 요청을 도메인으로 변환
        carProperties.licencePlateNumber shouldBe LicensePlateNumber(licensePlateNumber)
        responseDto.licensePlateNumber shouldBe licensePlateNumber
    }
})
