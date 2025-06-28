package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import java.time.Duration
import java.time.Instant

/**
 * 주차 기록을 나타내는 도메인 엔티티
 * 
 * 이 클래스는 차량의 주차 및 출차 정보를 관리합니다.
 * 도메인 주도 설계(DDD)의 엔티티(Entity) 패턴을 따릅니다.
 */
data class ParkingRecord(
    val licensePlateNumber: LicensePlateNumber,
    val parkingLotName: ParkingLotName,
    val parkedAt: Instant,
    val leftAt: Instant? = null
) {
    
    /**
     * 현재 주차 중인지 확인합니다.
     */
    val isParked: Boolean
        get() = leftAt == null
    
    /**
     * 차량을 출차 처리합니다.
     * 
     * @return 출차 시간이 기록된 새로운 ParkingRecord
     * @throws IllegalStateException 이미 출차한 차량인 경우
     */
    fun leave(): ParkingRecord {
        check(isParked) { "이미 출차한 차량입니다" }
        
        return this.copy(leftAt = Instant.now())
    }
    
    /**
     * 주차 시간을 계산합니다.
     * 
     * @return 주차 시간 (Duration)
     */
    fun getParkingDuration(): Duration {
        val endTime = leftAt ?: Instant.now()
        return Duration.between(parkedAt, endTime)
    }
}
