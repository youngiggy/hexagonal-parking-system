package com.example.hexagonal.car

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Instant
import java.util.UUID

class CarJpaEntityTest : StringSpec({
    "CarJpaEntity가 기본 생성자로 생성된다" {
        // when
        val entity = CarJpaEntity()
        
        // then
        entity.shouldBeInstanceOf<CarJpaEntity>()
        entity.id shouldNotBe null
        entity.id.shouldBeInstanceOf<UUID>()
        entity.licensePlateNumber shouldBe ""
        entity.createdAt shouldNotBe null
        entity.updatedAt shouldNotBe null
        entity.createdAt.shouldBeInstanceOf<Instant>()
        entity.updatedAt.shouldBeInstanceOf<Instant>()
    }
    
    "CarJpaEntity가 CarProperties로부터 생성된다" {
        // given
        val licensePlateNumber = "123가1234"
        val carData = CarData(LicensePlateNumber(licensePlateNumber))
        
        // when
        val entity = CarJpaEntity(carData)
        
        // then
        entity.licensePlateNumber shouldBe licensePlateNumber
        entity.id shouldNotBe null
        entity.id.shouldBeInstanceOf<UUID>()
        entity.createdAt shouldNotBe null
        entity.updatedAt shouldNotBe null
    }
    
    "CarJpaEntity를 CarModel로 변환한다" {
        // given
        val licensePlateNumber = "456나5678"
        val id = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        
        val entity = CarJpaEntity(
            licensePlateNumber = licensePlateNumber,
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
        
        // when
        val model = entity.toModel()
        
        // then
        model.shouldBeInstanceOf<CarModel>()
        model.shouldBeInstanceOf<CarEntity>()
        model.licencePlateNumber.value shouldBe licensePlateNumber
        model.licencePlateNumber.shouldBeInstanceOf<LicensePlateNumber>()
        model.identity.value shouldBe id
        model.identity.shouldBeInstanceOf<CarKey>()
        model.createdAt shouldBe createdAt
        model.updatedAt shouldBe updatedAt
    }
    
    "CarJpaEntity의 ID가 자동 생성된다" {
        // given
        val entity1 = CarJpaEntity()
        val entity2 = CarJpaEntity()
        
        // when & then
        entity1.id shouldNotBe entity2.id
        entity1.id.shouldBeInstanceOf<UUID>()
        entity2.id.shouldBeInstanceOf<UUID>()
        
        // UUID는 고유해야 함
        entity1.id.toString().length shouldBe 36 // UUID 표준 길이
        entity2.id.toString().length shouldBe 36
    }
    
    "CarJpaEntity의 타임스탬프가 자동 설정된다" {
        // given
        val beforeCreation = Instant.now()
        
        // when
        val entity = CarJpaEntity()
        
        // then
        val afterCreation = Instant.now()
        entity.createdAt shouldNotBe null
        entity.updatedAt shouldNotBe null
        
        // 생성 시간이 합리적인 범위 내에 있는지 확인
        entity.createdAt.isAfter(beforeCreation.minusSeconds(1)) shouldBe true
        entity.createdAt.isBefore(afterCreation.plusSeconds(1)) shouldBe true
        entity.updatedAt.isAfter(beforeCreation.minusSeconds(1)) shouldBe true
        entity.updatedAt.isBefore(afterCreation.plusSeconds(1)) shouldBe true
    }
    
    "CarJpaEntity에서 CarModel로 변환 시 도메인 객체가 올바르게 생성된다" {
        // given
        val licensePlateNumber = "789다7890"
        val carData = CarData(LicensePlateNumber(licensePlateNumber))
        val entity = CarJpaEntity(carData)
        
        // when
        val model = entity.toModel()
        
        // then
        model.licencePlateNumber.shouldBeInstanceOf<LicensePlateNumber>()
        model.identity.shouldBeInstanceOf<CarIdentity>()
        model.identity.shouldBeInstanceOf<CarKey>()
        model.licencePlateNumber.value shouldBe licensePlateNumber
        model.identity.value shouldBe entity.id
        
        // 도메인 모델의 타입 안전성 확인
        val carEntity: CarEntity = model as CarEntity
        carEntity.licencePlateNumber.value shouldBe licensePlateNumber
    }
    
    "동일한 ID를 가진 CarJpaEntity는 같은 ID를 가진다" {
        // given
        val id = UUID.randomUUID()
        val entity1 = CarJpaEntity(licensePlateNumber = "111가1111", id = id)
        val entity2 = CarJpaEntity(licensePlateNumber = "222나2222", id = id)
        
        // when & then
        entity1.id shouldBe entity2.id
        entity1.id shouldBe id
        entity2.id shouldBe id
    }
    
    "CarJpaEntity는 JPA 매핑 정보를 올바르게 가진다" {
        // given
        val licensePlateNumber = "JPA테스트"
        val entity = CarJpaEntity(licensePlateNumber = licensePlateNumber)
        
        // when & then - JPA 어노테이션이 적용된 엔티티
        entity.shouldBeInstanceOf<CarJpaEntity>()
        entity.licensePlateNumber shouldBe licensePlateNumber
        
        // 필드들이 올바르게 설정되어 있는지 확인
        entity.id shouldNotBe null
        entity.createdAt shouldNotBe null
        entity.updatedAt shouldNotBe null
    }
    
    "CarJpaEntity 변환 과정에서 데이터 무결성이 유지된다" {
        // given
        val originalLicensePlateNumber = "111가1111"
        val carData = CarData(LicensePlateNumber(originalLicensePlateNumber))
        
        // when
        val entity = CarJpaEntity(carData)
        val model = entity.toModel()
        val backToEntity = CarJpaEntity(model)
        
        // then - 원본 데이터가 보존되어야 함
        backToEntity.licensePlateNumber shouldBe originalLicensePlateNumber
        model.licencePlateNumber.value shouldBe originalLicensePlateNumber
        entity.licensePlateNumber shouldBe originalLicensePlateNumber
    }
    
    "CarJpaEntity는 다양한 번호판 형식을 지원한다" {
        // given
        val licensePlateNumbers = listOf(
            "서울 123 가 1234",
            "123가1234",
            "경기 456 나 5678",
            "1다1111"
        )
        
        // when & then
        licensePlateNumbers.forEach { plateNumber ->
            val carData = CarData(LicensePlateNumber(plateNumber))
            val entity = CarJpaEntity(carData)
            val model = entity.toModel()
            
            entity.licensePlateNumber shouldBe plateNumber
            model.licencePlateNumber.value shouldBe plateNumber
        }
    }
})
