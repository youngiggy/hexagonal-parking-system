package com.example.hexagonal.parkinglot

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 주차 기록 JPA 리포지토리
 * 
 * 이 인터페이스는 주차 기록 데이터에 대한 데이터베이스 접근을 제공합니다.
 * Spring Data JPA를 사용하여 자동으로 구현됩니다.
 */
@Repository
interface ParkingRecordJpaRepository : JpaRepository<ParkingRecordJpaEntity, Long> {
    
    /**
     * 번호판으로 현재 주차 중인 기록을 조회합니다.
     */
    fun findByLicensePlateNumberAndLeftAtIsNull(licensePlateNumber: String): ParkingRecordJpaEntity?
    
    /**
     * 주차장 이름으로 현재 주차 중인 차량들을 조회합니다.
     */
    fun findByParkingLotNameAndLeftAtIsNull(parkingLotName: String): List<ParkingRecordJpaEntity>
}
