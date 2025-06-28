package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ParkingLotPortTest : StringSpec({
    "ParkingLotLoadPort는 주차장을 조회할 수 있다" {
        // given
        val parkingLotName = ParkingLotName("로드 테스트 주차장")
        
        // when & then - 인터페이스 정의 확인
        val loadPort: ParkingLotLoadPort = object : ParkingLotLoadPort {
            override fun loadParkingLot(name: ParkingLotName): ParkingLot? {
                return ParkingLot(
                    name = name,
                    totalSpaces = ParkingSpaceCount(150)
                )
            }
            
            override fun loadParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord? {
                return null
            }
            
            override fun loadParkedCars(parkingLotName: ParkingLotName): List<ParkingRecord> {
                return emptyList()
            }
            
            override fun existsParkingLot(name: ParkingLotName): Boolean {
                return true
            }
        }
        
        val parkingLot = loadPort.loadParkingLot(parkingLotName)
        parkingLot shouldNotBe null
        parkingLot!!.shouldBeInstanceOf<ParkingLot>()
        parkingLot.name shouldBe parkingLotName
        parkingLot.totalSpaces.value shouldBe 150
    }
    
    "ParkingLotLoadPort는 주차 기록을 조회할 수 있다" {
        // given
        val licensePlateNumber = LicensePlateNumber("111가1111")
        
        // when & then - 인터페이스 정의 확인
        val loadPort: ParkingLotLoadPort = object : ParkingLotLoadPort {
            override fun loadParkingLot(name: ParkingLotName): ParkingLot? {
                return null
            }
            
            override fun loadParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord? {
                return ParkingRecord(
                    licensePlateNumber = licensePlateNumber,
                    parkingLotName = ParkingLotName("기록 로드 테스트 주차장"),
                    parkedAt = java.time.Instant.now().minusSeconds(1800)
                )
            }
            
            override fun loadParkedCars(parkingLotName: ParkingLotName): List<ParkingRecord> {
                return emptyList()
            }
            
            override fun existsParkingLot(name: ParkingLotName): Boolean {
                return false
            }
        }
        
        val parkingRecord = loadPort.loadParkingRecord(licensePlateNumber)
        parkingRecord shouldNotBe null
        parkingRecord!!.shouldBeInstanceOf<ParkingRecord>()
        parkingRecord.licensePlateNumber shouldBe licensePlateNumber
        parkingRecord.isParked shouldBe true
    }
    
    "ParkingLotLoadPort는 주차된 차량 목록을 조회할 수 있다" {
        // given
        val parkingLotName = ParkingLotName("차량 목록 로드 테스트 주차장")
        
        // when & then - 인터페이스 정의 확인
        val loadPort: ParkingLotLoadPort = object : ParkingLotLoadPort {
            override fun loadParkingLot(name: ParkingLotName): ParkingLot? {
                return null
            }
            
            override fun loadParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord? {
                return null
            }
            
            override fun loadParkedCars(parkingLotName: ParkingLotName): List<ParkingRecord> {
                return listOf(
                    ParkingRecord(
                        licensePlateNumber = LicensePlateNumber("222나2222"),
                        parkingLotName = parkingLotName,
                        parkedAt = java.time.Instant.now().minusSeconds(900)
                    ),
                    ParkingRecord(
                        licensePlateNumber = LicensePlateNumber("333다3333"),
                        parkingLotName = parkingLotName,
                        parkedAt = java.time.Instant.now().minusSeconds(1800)
                    )
                )
            }
            
            override fun existsParkingLot(name: ParkingLotName): Boolean {
                return true
            }
        }
        
        val parkedCars = loadPort.loadParkedCars(parkingLotName)
        parkedCars.size shouldBe 2
        parkedCars.forEach { record ->
            record.parkingLotName shouldBe parkingLotName
            record.isParked shouldBe true
        }
    }
    
    "ParkingLotSavePort는 주차장을 저장할 수 있다" {
        // given
        val parkingLot = ParkingLot(
            name = ParkingLotName("저장 테스트 주차장"),
            totalSpaces = ParkingSpaceCount(300)
        )
        
        // when & then - 인터페이스 정의 확인
        val savePort: ParkingLotSavePort = object : ParkingLotSavePort {
            override fun saveParkingLot(parkingLot: ParkingLot): ParkingLot {
                return parkingLot
            }
            
            override fun saveParkingRecord(parkingRecord: ParkingRecord): ParkingRecord {
                return parkingRecord
            }
            
            override fun updateParkingRecord(parkingRecord: ParkingRecord): ParkingRecord {
                return parkingRecord
            }
            
            override fun deleteParkingLot(name: ParkingLotName): Boolean {
                return true
            }
        }
        
        val savedParkingLot = savePort.saveParkingLot(parkingLot)
        savedParkingLot.shouldBeInstanceOf<ParkingLot>()
        savedParkingLot.name shouldBe parkingLot.name
        savedParkingLot.totalSpaces shouldBe parkingLot.totalSpaces
    }
    
    "ParkingLotSavePort는 주차 기록을 저장할 수 있다" {
        // given
        val parkingRecord = ParkingRecord(
            licensePlateNumber = LicensePlateNumber("444라4444"),
            parkingLotName = ParkingLotName("기록 저장 테스트 주차장"),
            parkedAt = java.time.Instant.now()
        )
        
        // when & then - 인터페이스 정의 확인
        val savePort: ParkingLotSavePort = object : ParkingLotSavePort {
            override fun saveParkingLot(parkingLot: ParkingLot): ParkingLot {
                return parkingLot
            }
            
            override fun saveParkingRecord(parkingRecord: ParkingRecord): ParkingRecord {
                return parkingRecord
            }
            
            override fun updateParkingRecord(parkingRecord: ParkingRecord): ParkingRecord {
                return parkingRecord
            }
            
            override fun deleteParkingLot(name: ParkingLotName): Boolean {
                return false
            }
        }
        
        val savedRecord = savePort.saveParkingRecord(parkingRecord)
        savedRecord.shouldBeInstanceOf<ParkingRecord>()
        savedRecord.licensePlateNumber shouldBe parkingRecord.licensePlateNumber
        savedRecord.parkingLotName shouldBe parkingRecord.parkingLotName
        savedRecord.isParked shouldBe true
    }
    
    "ParkingLotSavePort는 주차 기록을 업데이트할 수 있다" {
        // given
        val originalRecord = ParkingRecord(
            licensePlateNumber = LicensePlateNumber("555마5555"),
            parkingLotName = ParkingLotName("업데이트 테스트 주차장"),
            parkedAt = java.time.Instant.now().minusSeconds(3600)
        )
        val updatedRecord = originalRecord.leave()
        
        // when & then - 인터페이스 정의 확인
        val savePort: ParkingLotSavePort = object : ParkingLotSavePort {
            override fun saveParkingLot(parkingLot: ParkingLot): ParkingLot {
                return parkingLot
            }
            
            override fun saveParkingRecord(parkingRecord: ParkingRecord): ParkingRecord {
                return parkingRecord
            }
            
            override fun updateParkingRecord(parkingRecord: ParkingRecord): ParkingRecord {
                return parkingRecord
            }
            
            override fun deleteParkingLot(name: ParkingLotName): Boolean {
                return true
            }
        }
        
        val result = savePort.updateParkingRecord(updatedRecord)
        result.shouldBeInstanceOf<ParkingRecord>()
        result.licensePlateNumber shouldBe updatedRecord.licensePlateNumber
        result.leftAt shouldNotBe null
        result.isParked shouldBe false
    }
    
    "ParkingLotLoadPort는 주차장 존재 여부를 확인할 수 있다" {
        // given
        val existingParkingLot = ParkingLotName("존재하는 주차장")
        val nonExistingParkingLot = ParkingLotName("존재하지 않는 주차장")
        
        // when & then - 인터페이스 정의 확인
        val loadPort: ParkingLotLoadPort = object : ParkingLotLoadPort {
            override fun loadParkingLot(name: ParkingLotName): ParkingLot? {
                return if (name == existingParkingLot) {
                    ParkingLot(name = name, totalSpaces = ParkingSpaceCount(100))
                } else null
            }
            
            override fun loadParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord? {
                return null
            }
            
            override fun loadParkedCars(parkingLotName: ParkingLotName): List<ParkingRecord> {
                return emptyList()
            }
            
            override fun existsParkingLot(name: ParkingLotName): Boolean {
                return name == existingParkingLot
            }
        }
        
        loadPort.existsParkingLot(existingParkingLot) shouldBe true
        loadPort.existsParkingLot(nonExistingParkingLot) shouldBe false
    }
})
