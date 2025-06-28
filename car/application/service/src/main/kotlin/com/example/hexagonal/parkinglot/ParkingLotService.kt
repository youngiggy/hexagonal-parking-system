package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 주차장 관리 애플리케이션 서비스
 * 
 * 이 클래스는 주차장 관련 비즈니스 로직을 조합하고 트랜잭션을 관리합니다.
 * 헥사고날 아키텍처에서 Application Service 역할을 하며,
 * 인바운드 포트를 구현하고 아웃바운드 포트를 사용합니다.
 */
@Service
@Transactional
class ParkingLotService(
    private val loadPort: ParkingLotLoadPort,
    private val savePort: ParkingLotSavePort
) : ParkingLotQueryUseCase, ParkingLotCommandUseCase {
    
    /**
     * 새로운 주차장을 생성합니다.
     */
    override fun createParkingLot(name: ParkingLotName, totalSpaces: ParkingSpaceCount): ParkingLot {
        // 이미 존재하는 주차장인지 확인
        if (loadPort.existsParkingLot(name)) {
            throw ParkingLotAlreadyExistsException("이미 존재하는 주차장입니다: ${name.value}")
        }
        
        // 새로운 주차장 생성
        val parkingLot = ParkingLot(
            name = name,
            totalSpaces = totalSpaces
        )
        
        // 주차장 저장
        return savePort.saveParkingLot(parkingLot)
    }
    
    /**
     * 주차장 상태를 조회합니다.
     */
    @Transactional(readOnly = true)
    override fun getParkingLotStatus(name: ParkingLotName): ParkingLotStatus {
        // 주차장 조회
        val parkingLot = loadPort.loadParkingLot(name)
            ?: throw ParkingLotNotFoundException("주차장을 찾을 수 없습니다: ${name.value}")
        
        // 현재 주차된 차량 목록 조회
        val parkedCars = loadPort.loadParkedCars(name)
        
        // 주차장 상태 계산
        val occupiedSpaces = ParkingSpaceCount(parkedCars.size)
        val availableSpaces = parkingLot.totalSpaces - occupiedSpaces
        val occupancyRate = if (parkingLot.totalSpaces.value > 0) {
            occupiedSpaces.value.toDouble() / parkingLot.totalSpaces.value.toDouble()
        } else {
            0.0
        }
        
        return ParkingLotStatus(
            name = name,
            totalSpaces = parkingLot.totalSpaces,
            availableSpaces = availableSpaces,
            occupiedSpaces = occupiedSpaces,
            occupancyRate = occupancyRate
        )
    }
    
    /**
     * 주차장에 현재 주차된 차량 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    override fun getParkedCars(name: ParkingLotName): List<ParkingRecord> {
        return loadPort.loadParkedCars(name)
    }
    
    /**
     * 특정 차량의 주차 기록을 조회합니다.
     */
    @Transactional(readOnly = true)
    override fun findParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord? {
        return loadPort.loadParkingRecord(licensePlateNumber)
    }
    
    /**
     * 차량을 주차합니다.
     */
    override fun parkCar(parkingLotName: ParkingLotName, licensePlateNumber: LicensePlateNumber): ParkingRecord {
        // 주차장 존재 확인
        val parkingLot = loadPort.loadParkingLot(parkingLotName)
            ?: throw ParkingLotNotFoundException("주차장을 찾을 수 없습니다: ${parkingLotName.value}")
        
        // 이미 주차된 차량인지 확인
        val existingRecord = loadPort.loadParkingRecord(licensePlateNumber)
        if (existingRecord != null && existingRecord.isParked) {
            throw CarAlreadyParkedException("이미 주차된 차량입니다: ${licensePlateNumber.value}")
        }
        
        // 주차 공간 확인
        val parkedCars = loadPort.loadParkedCars(parkingLotName)
        if (parkedCars.size >= parkingLot.totalSpaces.value) {
            throw ParkingLotFullException("주차장이 만차입니다: ${parkingLotName.value}")
        }
        
        // 주차 기록 생성
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = parkingLotName,
            parkedAt = java.time.Instant.now()
        )
        
        // 주차 기록 저장
        return savePort.saveParkingRecord(parkingRecord)
    }
    
    /**
     * 차량을 출차합니다.
     */
    override fun leaveCar(licensePlateNumber: LicensePlateNumber): ParkingRecord {
        // 주차 기록 조회
        val parkingRecord = loadPort.loadParkingRecord(licensePlateNumber)
            ?: throw CarNotParkedException("주차되지 않은 차량입니다: ${licensePlateNumber.value}")
        
        // 이미 출차한 차량인지 확인
        if (!parkingRecord.isParked) {
            throw CarNotParkedException("이미 출차한 차량입니다: ${licensePlateNumber.value}")
        }
        
        // 출차 처리
        val leftRecord = parkingRecord.leave()
        
        // 출차 기록 업데이트
        return savePort.updateParkingRecord(leftRecord)
    }
}
