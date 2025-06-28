package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ParkingLotUseCaseTest : StringSpec({
    "ParkingLotQueryUseCase는 주차장 상태를 조회할 수 있다" {
        // given
        val parkingLotName = ParkingLotName("테스트 주차장")
        
        // when & then - 인터페이스 정의 확인
        val queryUseCase: ParkingLotQueryUseCase = object : ParkingLotQueryUseCase {
            override fun getParkingLotStatus(name: ParkingLotName): ParkingLotStatus {
                return ParkingLotStatus(
                    name = name,
                    totalSpaces = ParkingSpaceCount(100),
                    availableSpaces = ParkingSpaceCount(80),
                    occupiedSpaces = ParkingSpaceCount(20),
                    occupancyRate = 0.2
                )
            }
            
            override fun getParkedCars(name: ParkingLotName): List<ParkingRecord> {
                return emptyList()
            }
            
            override fun findParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord? {
                return null
            }
        }
        
        val status = queryUseCase.getParkingLotStatus(parkingLotName)
        status.shouldBeInstanceOf<ParkingLotStatus>()
        status.name shouldBe parkingLotName
        status.totalSpaces.value shouldBe 100
        status.availableSpaces.value shouldBe 80
        status.occupiedSpaces.value shouldBe 20
        status.occupancyRate shouldBe 0.2
    }
    
    "ParkingLotCommandUseCase는 차량을 주차할 수 있다" {
        // given
        val parkingLotName = ParkingLotName("명령 테스트 주차장")
        val licensePlateNumber = LicensePlateNumber("123가1234")
        
        // when & then - 인터페이스 정의 확인
        val commandUseCase: ParkingLotCommandUseCase = object : ParkingLotCommandUseCase {
            override fun parkCar(parkingLotName: ParkingLotName, licensePlateNumber: LicensePlateNumber): ParkingRecord {
                return ParkingRecord(
                    licensePlateNumber = licensePlateNumber,
                    parkingLotName = parkingLotName,
                    parkedAt = java.time.Instant.now()
                )
            }
            
            override fun leaveCar(licensePlateNumber: LicensePlateNumber): ParkingRecord {
                return ParkingRecord(
                    licensePlateNumber = licensePlateNumber,
                    parkingLotName = parkingLotName,
                    parkedAt = java.time.Instant.now().minusSeconds(3600),
                    leftAt = java.time.Instant.now()
                )
            }
            
            override fun createParkingLot(name: ParkingLotName, totalSpaces: ParkingSpaceCount): ParkingLot {
                return ParkingLot(name = name, totalSpaces = totalSpaces)
            }
        }
        
        val parkingRecord = commandUseCase.parkCar(parkingLotName, licensePlateNumber)
        parkingRecord.shouldBeInstanceOf<ParkingRecord>()
        parkingRecord.licensePlateNumber shouldBe licensePlateNumber
        parkingRecord.parkingLotName shouldBe parkingLotName
        parkingRecord.parkedAt shouldNotBe null
        parkingRecord.leftAt shouldBe null
    }
    
    "ParkingLotCommandUseCase는 차량을 출차할 수 있다" {
        // given
        val licensePlateNumber = LicensePlateNumber("456나5678")
        
        // when & then - 인터페이스 정의 확인
        val commandUseCase: ParkingLotCommandUseCase = object : ParkingLotCommandUseCase {
            override fun parkCar(parkingLotName: ParkingLotName, licensePlateNumber: LicensePlateNumber): ParkingRecord {
                return ParkingRecord(
                    licensePlateNumber = licensePlateNumber,
                    parkingLotName = parkingLotName,
                    parkedAt = java.time.Instant.now()
                )
            }
            
            override fun leaveCar(licensePlateNumber: LicensePlateNumber): ParkingRecord {
                return ParkingRecord(
                    licensePlateNumber = licensePlateNumber,
                    parkingLotName = ParkingLotName("출차 테스트 주차장"),
                    parkedAt = java.time.Instant.now().minusSeconds(3600),
                    leftAt = java.time.Instant.now()
                )
            }
            
            override fun createParkingLot(name: ParkingLotName, totalSpaces: ParkingSpaceCount): ParkingLot {
                return ParkingLot(name = name, totalSpaces = totalSpaces)
            }
        }
        
        val leftRecord = commandUseCase.leaveCar(licensePlateNumber)
        leftRecord.shouldBeInstanceOf<ParkingRecord>()
        leftRecord.licensePlateNumber shouldBe licensePlateNumber
        leftRecord.leftAt shouldNotBe null
        leftRecord.isParked shouldBe false
    }
    
    "ParkingLotCommandUseCase는 주차장을 생성할 수 있다" {
        // given
        val parkingLotName = ParkingLotName("새로운 주차장")
        val totalSpaces = ParkingSpaceCount(200)
        
        // when & then - 인터페이스 정의 확인
        val commandUseCase: ParkingLotCommandUseCase = object : ParkingLotCommandUseCase {
            override fun parkCar(parkingLotName: ParkingLotName, licensePlateNumber: LicensePlateNumber): ParkingRecord {
                return ParkingRecord(
                    licensePlateNumber = licensePlateNumber,
                    parkingLotName = parkingLotName,
                    parkedAt = java.time.Instant.now()
                )
            }
            
            override fun leaveCar(licensePlateNumber: LicensePlateNumber): ParkingRecord {
                return ParkingRecord(
                    licensePlateNumber = licensePlateNumber,
                    parkingLotName = parkingLotName,
                    parkedAt = java.time.Instant.now().minusSeconds(3600),
                    leftAt = java.time.Instant.now()
                )
            }
            
            override fun createParkingLot(name: ParkingLotName, totalSpaces: ParkingSpaceCount): ParkingLot {
                return ParkingLot(name = name, totalSpaces = totalSpaces)
            }
        }
        
        val parkingLot = commandUseCase.createParkingLot(parkingLotName, totalSpaces)
        parkingLot.shouldBeInstanceOf<ParkingLot>()
        parkingLot.name shouldBe parkingLotName
        parkingLot.totalSpaces shouldBe totalSpaces
        parkingLot.availableSpaces shouldBe totalSpaces
        parkingLot.occupiedSpaces.value shouldBe 0
    }
    
    "ParkingLotQueryUseCase는 주차된 차량 목록을 조회할 수 있다" {
        // given
        val parkingLotName = ParkingLotName("차량 목록 테스트 주차장")
        
        // when & then - 인터페이스 정의 확인
        val queryUseCase: ParkingLotQueryUseCase = object : ParkingLotQueryUseCase {
            override fun getParkingLotStatus(name: ParkingLotName): ParkingLotStatus {
                return ParkingLotStatus(
                    name = name,
                    totalSpaces = ParkingSpaceCount(100),
                    availableSpaces = ParkingSpaceCount(97),
                    occupiedSpaces = ParkingSpaceCount(3),
                    occupancyRate = 0.03
                )
            }
            
            override fun getParkedCars(name: ParkingLotName): List<ParkingRecord> {
                return listOf(
                    ParkingRecord(
                        licensePlateNumber = LicensePlateNumber("111가1111"),
                        parkingLotName = name,
                        parkedAt = java.time.Instant.now().minusSeconds(1800)
                    ),
                    ParkingRecord(
                        licensePlateNumber = LicensePlateNumber("222나2222"),
                        parkingLotName = name,
                        parkedAt = java.time.Instant.now().minusSeconds(3600)
                    )
                )
            }
            
            override fun findParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord? {
                return null
            }
        }
        
        val parkedCars = queryUseCase.getParkedCars(parkingLotName)
        parkedCars.size shouldBe 2
        parkedCars.forEach { record ->
            record.parkingLotName shouldBe parkingLotName
            record.leftAt shouldBe null
            record.isParked shouldBe true
        }
    }
    
    "ParkingLotQueryUseCase는 특정 차량의 주차 기록을 조회할 수 있다" {
        // given
        val licensePlateNumber = LicensePlateNumber("789다7890")
        
        // when & then - 인터페이스 정의 확인
        val queryUseCase: ParkingLotQueryUseCase = object : ParkingLotQueryUseCase {
            override fun getParkingLotStatus(name: ParkingLotName): ParkingLotStatus {
                return ParkingLotStatus(
                    name = name,
                    totalSpaces = ParkingSpaceCount(100),
                    availableSpaces = ParkingSpaceCount(99),
                    occupiedSpaces = ParkingSpaceCount(1),
                    occupancyRate = 0.01
                )
            }
            
            override fun getParkedCars(name: ParkingLotName): List<ParkingRecord> {
                return emptyList()
            }
            
            override fun findParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord? {
                return ParkingRecord(
                    licensePlateNumber = licensePlateNumber,
                    parkingLotName = ParkingLotName("기록 조회 테스트 주차장"),
                    parkedAt = java.time.Instant.now().minusSeconds(7200)
                )
            }
        }
        
        val parkingRecord = queryUseCase.findParkingRecord(licensePlateNumber)
        parkingRecord shouldNotBe null
        parkingRecord!!.licensePlateNumber shouldBe licensePlateNumber
        parkingRecord.isParked shouldBe true
    }
})
