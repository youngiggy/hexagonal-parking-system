package com.example.hexagonal.parkinglot

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 주차장 JPA 리포지토리
 * 
 * 이 인터페이스는 주차장 데이터에 대한 데이터베이스 접근을 제공합니다.
 * Spring Data JPA를 사용하여 자동으로 구현됩니다.
 */
@Repository
interface ParkingLotJpaRepository : JpaRepository<ParkingLotJpaEntity, Long> {
    
    /**
     * 이름으로 주차장을 조회합니다.
     */
    fun findByName(name: String): ParkingLotJpaEntity?
    
    /**
     * 이름으로 주차장 존재 여부를 확인합니다.
     */
    fun existsByName(name: String): Boolean
    
    /**
     * 이름으로 주차장을 삭제합니다.
     */
    fun deleteByName(name: String)
}
