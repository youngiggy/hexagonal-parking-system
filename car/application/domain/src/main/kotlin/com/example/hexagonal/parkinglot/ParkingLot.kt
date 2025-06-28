package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import java.time.Instant

/**
 * 주차장을 나타내는 도메인 엔티티
 * 
 * 이 클래스는 주차장의 상태와 주차/출차 로직을 관리합니다.
 * 도메인 주도 설계(DDD)의 애그리게이트 루트(Aggregate Root) 역할을 합니다.
 */
data class ParkingLot(
    val name: ParkingLotName,
    val totalSpaces: ParkingSpaceCount,
    private val parkedCars: MutableMap<LicensePlateNumber, ParkingRecord> = mutableMapOf()
) {
    
    /**
     * 현재 사용 가능한 주차 공간 수
     */
    val availableSpaces: ParkingSpaceCount
        get() = totalSpaces - occupiedSpaces
    
    /**
     * 현재 점유된 주차 공간 수
     */
    val occupiedSpaces: ParkingSpaceCount
        get() = ParkingSpaceCount(parkedCars.size)
    
    /**
     * 차량을 주차합니다.
     * 
     * @param licensePlateNumber 주차할 차량의 번호판
     * @return 주차 기록
     * @throws ParkingLotFullException 주차장이 만차인 경우
     * @throws CarAlreadyParkedException 이미 주차된 차량인 경우
     */
    fun parkCar(licensePlateNumber: LicensePlateNumber): ParkingRecord {
        require(availableSpaces.value > 0) { "주차장이 만차입니다" }
        require(!parkedCars.containsKey(licensePlateNumber)) { "이미 주차된 차량입니다" }
        
        val parkingRecord = ParkingRecord(
            licensePlateNumber = licensePlateNumber,
            parkingLotName = name,
            parkedAt = Instant.now()
        )
        
        parkedCars[licensePlateNumber] = parkingRecord
        return parkingRecord
    }
    
    /**
     * 차량을 출차합니다.
     * 
     * @param licensePlateNumber 출차할 차량의 번호판
     * @return 출차 기록
     * @throws CarNotParkedException 주차되지 않은 차량인 경우
     */
    fun leaveCar(licensePlateNumber: LicensePlateNumber): ParkingRecord {
        val parkingRecord = parkedCars[licensePlateNumber]
            ?: throw IllegalArgumentException("주차되지 않은 차량입니다")
        
        val leftRecord = parkingRecord.leave()
        parkedCars.remove(licensePlateNumber)
        return leftRecord
    }
    
    /**
     * 주차장 상태를 조회합니다.
     * 
     * @return 주차장 상태 정보
     */
    fun getStatus(): ParkingLotStatus {
        return ParkingLotStatus(
            name = name,
            totalSpaces = totalSpaces,
            availableSpaces = availableSpaces,
            occupiedSpaces = occupiedSpaces,
            occupancyRate = occupiedSpaces.value.toDouble() / totalSpaces.value.toDouble()
        )
    }
    
    /**
     * 현재 주차된 차량 목록을 조회합니다.
     * 
     * @return 주차된 차량들의 주차 기록 목록
     */
    fun getParkedCars(): List<ParkingRecord> {
        return parkedCars.values.toList()
    }
}
