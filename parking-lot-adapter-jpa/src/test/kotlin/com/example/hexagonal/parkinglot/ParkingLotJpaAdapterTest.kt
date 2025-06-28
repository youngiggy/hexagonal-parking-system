package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.util.*

class ParkingLotJpaAdapterTest : StringSpec({
    "ParkingLotJpaAdapter는 주차장을 저장할 수 있다" {
        // given
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val adapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        val parkingLot = ParkingLot(
            name = ParkingLotName("저장 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(100)
        )
        val savedEntity = ParkingLotJpaEntity.fromDomainModel(parkingLot).apply { id = 1L }
        
        every { parkingLotRepository.save(any()) } returns savedEntity
        
        // when
        val result = adapter.saveParkingLot(parkingLot)
        
        // then
        result.shouldBeInstanceOf<ParkingLot>()
        result.name shouldBe parkingLot.name
        result.totalSpaces shouldBe parkingLot.totalSpaces
        
        verify(exactly = 1) { parkingLotRepository.save(any()) }
    }
    
    "ParkingLotJpaAdapter는 주차장을 조회할 수 있다" {
        // given
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val adapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        val parkingLotName = ParkingLotName("조회 테스트 주차장")
        val entity = ParkingLotJpaEntity(
            name = parkingLotName.value,
            totalSpaces = 150
        ).apply { id = 1L }
        
        every { parkingLotRepository.findByName(parkingLotName.value) } returns entity
        
        // when
        val result = adapter.loadParkingLot(parkingLotName)
        
        // then
        result shouldNotBe null
        result!!.shouldBeInstanceOf<ParkingLot>()
        result.name shouldBe parkingLotName
        result.totalSpaces.value shouldBe 150
        
        verify(exactly = 1) { parkingLotRepository.findByName(parkingLotName.value) }
    }
    
    "ParkingLotJpaAdapter는 존재하지 않는 주차장 조회시 null을 반환한다" {
        // given
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val adapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        val parkingLotName = ParkingLotName("존재하지 않는 주차장")
        
        every { parkingLotRepository.findByName(parkingLotName.value) } returns null
        
        // when
        val result = adapter.loadParkingLot(parkingLotName)
        
        // then
        result shouldBe null
        
        verify(exactly = 1) { parkingLotRepository.findByName(parkingLotName.value) }
    }
    
    "ParkingLotJpaAdapter는 주차장 존재 여부를 확인할 수 있다" {
        // given
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val adapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        val existingParkingLot = ParkingLotName("존재하는 주차장")
        val nonExistingParkingLot = ParkingLotName("존재하지 않는 주차장")
        
        every { parkingLotRepository.existsByName(existingParkingLot.value) } returns true
        every { parkingLotRepository.existsByName(nonExistingParkingLot.value) } returns false
        
        // when & then
        adapter.existsParkingLot(existingParkingLot) shouldBe true
        adapter.existsParkingLot(nonExistingParkingLot) shouldBe false
        
        verify(exactly = 1) { parkingLotRepository.existsByName(existingParkingLot.value) }
        verify(exactly = 1) { parkingLotRepository.existsByName(nonExistingParkingLot.value) }
    }
    
    "ParkingLotJpaAdapter는 주차 기록을 저장할 수 있다" {
        // given
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val adapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        val parkingRecord = ParkingRecord(
            licensePlateNumber = LicensePlateNumber("123가1234"),
            parkingLotName = ParkingLotName("기록 저장 테스트 주차장"),
            parkedAt = Instant.now()
        )
        val savedEntity = ParkingRecordJpaEntity.fromDomainModel(parkingRecord).apply { id = 1L }
        
        every { parkingRecordRepository.save(any()) } returns savedEntity
        
        // when
        val result = adapter.saveParkingRecord(parkingRecord)
        
        // then
        result.shouldBeInstanceOf<ParkingRecord>()
        result.licensePlateNumber shouldBe parkingRecord.licensePlateNumber
        result.parkingLotName shouldBe parkingRecord.parkingLotName
        result.isParked shouldBe true
        
        verify(exactly = 1) { parkingRecordRepository.save(any()) }
    }
    
    "ParkingLotJpaAdapter는 주차 기록을 조회할 수 있다" {
        // given
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val adapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        val licensePlateNumber = LicensePlateNumber("456나5678")
        val entity = ParkingRecordJpaEntity(
            licensePlateNumber = licensePlateNumber.value,
            parkingLotName = "기록 조회 테스트 주차장",
            parkedAt = Instant.now().minusSeconds(3600)
        ).apply { id = 1L }
        
        every { parkingRecordRepository.findByLicensePlateNumberAndLeftAtIsNull(licensePlateNumber.value) } returns entity
        
        // when
        val result = adapter.loadParkingRecord(licensePlateNumber)
        
        // then
        result shouldNotBe null
        result!!.shouldBeInstanceOf<ParkingRecord>()
        result.licensePlateNumber shouldBe licensePlateNumber
        result.isParked shouldBe true
        
        verify(exactly = 1) { parkingRecordRepository.findByLicensePlateNumberAndLeftAtIsNull(licensePlateNumber.value) }
    }
    
    "ParkingLotJpaAdapter는 주차된 차량 목록을 조회할 수 있다" {
        // given
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val adapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        val parkingLotName = ParkingLotName("차량 목록 조회 테스트 주차장")
        val entities = listOf(
            ParkingRecordJpaEntity(
                licensePlateNumber = "111가1111",
                parkingLotName = parkingLotName.value,
                parkedAt = Instant.now().minusSeconds(1800)
            ).apply { id = 1L },
            ParkingRecordJpaEntity(
                licensePlateNumber = "222나2222",
                parkingLotName = parkingLotName.value,
                parkedAt = Instant.now().minusSeconds(3600)
            ).apply { id = 2L }
        )
        
        every { parkingRecordRepository.findByParkingLotNameAndLeftAtIsNull(parkingLotName.value) } returns entities
        
        // when
        val result = adapter.loadParkedCars(parkingLotName)
        
        // then
        result.size shouldBe 2
        result.forEach { record ->
            record.parkingLotName shouldBe parkingLotName
            record.isParked shouldBe true
        }
        
        verify(exactly = 1) { parkingRecordRepository.findByParkingLotNameAndLeftAtIsNull(parkingLotName.value) }
    }
    
    "ParkingLotJpaAdapter는 주차 기록을 업데이트할 수 있다" {
        // given
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val adapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        val originalRecord = ParkingRecord(
            licensePlateNumber = LicensePlateNumber("789다7890"),
            parkingLotName = ParkingLotName("업데이트 테스트 주차장"),
            parkedAt = Instant.now().minusSeconds(3600)
        )
        val updatedRecord = originalRecord.leave()
        val updatedEntity = ParkingRecordJpaEntity.fromDomainModel(updatedRecord).apply { id = 1L }
        
        every { parkingRecordRepository.save(any()) } returns updatedEntity
        
        // when
        val result = adapter.updateParkingRecord(updatedRecord)
        
        // then
        result.shouldBeInstanceOf<ParkingRecord>()
        result.licensePlateNumber shouldBe updatedRecord.licensePlateNumber
        result.leftAt shouldNotBe null
        result.isParked shouldBe false
        
        verify(exactly = 1) { parkingRecordRepository.save(any()) }
    }
    
    "ParkingLotJpaAdapter는 주차장을 삭제할 수 있다" {
        // given
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val adapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        val parkingLotName = ParkingLotName("삭제 테스트 주차장")
        
        every { parkingLotRepository.existsByName(parkingLotName.value) } returns true
        every { parkingLotRepository.deleteByName(parkingLotName.value) } returns Unit
        
        // when
        val result = adapter.deleteParkingLot(parkingLotName)
        
        // then
        result shouldBe true
        
        verify(exactly = 1) { parkingLotRepository.existsByName(parkingLotName.value) }
        verify(exactly = 1) { parkingLotRepository.deleteByName(parkingLotName.value) }
    }
    
    "ParkingLotJpaAdapter는 존재하지 않는 주차장 삭제시 false를 반환한다" {
        // given
        val parkingLotRepository = mockk<ParkingLotJpaRepository>()
        val parkingRecordRepository = mockk<ParkingRecordJpaRepository>()
        val adapter = ParkingLotJpaAdapter(parkingLotRepository, parkingRecordRepository)
        
        val parkingLotName = ParkingLotName("존재하지 않는 주차장")
        
        every { parkingLotRepository.existsByName(parkingLotName.value) } returns false
        
        // when
        val result = adapter.deleteParkingLot(parkingLotName)
        
        // then
        result shouldBe false
        
        verify(exactly = 1) { parkingLotRepository.existsByName(parkingLotName.value) }
        verify(exactly = 0) { parkingLotRepository.deleteByName(any()) }
    }
})
