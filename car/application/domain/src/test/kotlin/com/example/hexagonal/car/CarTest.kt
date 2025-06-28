package com.example.hexagonal.car

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Instant
import java.util.UUID

class CarTest : StringSpec({
    "CarData가 생성된다" {
        // given
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        
        // when
        val carData = CarData(licensePlateNumber)
        
        // then
        carData.licencePlateNumber shouldBe licensePlateNumber
        carData.shouldBeInstanceOf<CarProperties>()
    }
    
    "CarKey가 생성된다" {
        // given
        val uuid = UUID.randomUUID()
        
        // when
        val carKey = CarKey(uuid)
        
        // then
        carKey.value shouldBe uuid
        carKey.shouldBeInstanceOf<CarIdentity>()
    }
    
    "CarEntity가 생성된다" {
        // given
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val carKey = CarKey(UUID.randomUUID())
        val now = Instant.now()
        
        // when
        val carEntity = CarEntity(
            licencePlateNumber = licensePlateNumber,
            identity = carKey,
            createdAt = now,
            updatedAt = now
        )
        
        // then
        carEntity.licencePlateNumber shouldBe licensePlateNumber
        carEntity.identity shouldBe carKey
        carEntity.createdAt shouldBe now
        carEntity.updatedAt shouldBe now
        carEntity.shouldBeInstanceOf<CarModel>()
        carEntity.shouldBeInstanceOf<CarProperties>()
    }
    
    "CarEntity는 CarModel 인터페이스를 구현한다" {
        // given
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val carKey = CarKey(UUID.randomUUID())
        val now = Instant.now()
        
        // when
        val carEntity = CarEntity(
            licencePlateNumber = licensePlateNumber,
            identity = carKey,
            createdAt = now,
            updatedAt = now
        )
        
        // then
        val carModel: CarModel = carEntity // 타입 체크
        carModel.licencePlateNumber shouldBe licensePlateNumber
        carModel.identity shouldBe carKey
        carModel.createdAt shouldBe now
        carModel.updatedAt shouldBe now
    }
    
    "CarData는 CarProperties 인터페이스를 구현한다" {
        // given
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        
        // when
        val carData = CarData(licensePlateNumber)
        
        // then
        val carProperties: CarProperties = carData // 타입 체크
        carProperties.licencePlateNumber shouldBe licensePlateNumber
    }
    
    "CarKey는 CarIdentity 인터페이스를 구현한다" {
        // given
        val uuid = UUID.randomUUID()
        
        // when
        val carKey = CarKey(uuid)
        
        // then
        val carIdentity: CarIdentity = carKey // 타입 체크
        carIdentity.value shouldBe uuid
    }
    
    "동일한 CarKey는 같다고 판단된다" {
        // given
        val uuid = UUID.randomUUID()
        val carKey1 = CarKey(uuid)
        val carKey2 = CarKey(uuid)
        
        // when & then
        carKey1 shouldBe carKey2
        carKey1.hashCode() shouldBe carKey2.hashCode()
    }
    
    "다른 CarKey는 다르다고 판단된다" {
        // given
        val carKey1 = CarKey(UUID.randomUUID())
        val carKey2 = CarKey(UUID.randomUUID())
        
        // when & then
        carKey1 shouldNotBe carKey2
        carKey1.hashCode() shouldNotBe carKey2.hashCode()
    }
    
    "동일한 번호판을 가진 CarData는 같다고 판단된다" {
        // given
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val carData1 = CarData(licensePlateNumber)
        val carData2 = CarData(licensePlateNumber)
        
        // when & then
        carData1 shouldBe carData2
        carData1.hashCode() shouldBe carData2.hashCode()
    }
    
    "동일한 identity를 가진 CarEntity는 같다고 판단된다" {
        // given
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val carKey = CarKey(UUID.randomUUID())
        val now = Instant.now()
        
        val carEntity1 = CarEntity(licensePlateNumber, carKey, now, now)
        val carEntity2 = CarEntity(licensePlateNumber, carKey, now, now)
        
        // when & then
        carEntity1 shouldBe carEntity2
        carEntity1.hashCode() shouldBe carEntity2.hashCode()
    }
    
    "다른 identity를 가진 CarEntity는 다르다고 판단된다" {
        // given
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val carKey1 = CarKey(UUID.randomUUID())
        val carKey2 = CarKey(UUID.randomUUID())
        val now = Instant.now()
        
        val carEntity1 = CarEntity(licensePlateNumber, carKey1, now, now)
        val carEntity2 = CarEntity(licensePlateNumber, carKey2, now, now)
        
        // when & then
        carEntity1 shouldNotBe carEntity2
    }
    
    "CarModel은 CarProperties를 상속한다" {
        // given
        val licensePlateNumber = LicensePlateNumber("서울 123 가 1234")
        val carKey = CarKey(UUID.randomUUID())
        val now = Instant.now()
        val carEntity = CarEntity(licensePlateNumber, carKey, now, now)
        
        // when
        val carModel: CarModel = carEntity
        val carProperties: CarProperties = carModel
        
        // then
        carProperties.licencePlateNumber shouldBe licensePlateNumber
    }
})
