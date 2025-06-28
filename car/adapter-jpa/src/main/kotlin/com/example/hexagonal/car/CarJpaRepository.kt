package com.example.hexagonal.car

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * 차량 정보에 대한 JPA 리포지토리
 * 
 * 이 인터페이스는 Spring Data JPA를 사용하여 데이터베이스 CRUD 작업을 처리합니다.
 * 헥사고날 아키텍처에서 Secondary Adapter의 일부로 사용됩니다.
 */
@Repository
interface CarJpaRepository : JpaRepository<CarJpaEntity, UUID> {
    
    /**
     * 번호판으로 차량을 조회합니다.
     * 
     * @param licensePlateNumber 조회할 차량의 번호판
     * @return 조회된 차량 엔티티, 존재하지 않으면 null
     */
    fun findByLicensePlateNumber(licensePlateNumber: String): CarJpaEntity?
    
    /**
     * 번호판으로 차량 존재 여부를 확인합니다.
     * 
     * @param licensePlateNumber 확인할 차량의 번호판
     * @return 존재하면 true, 그렇지 않으면 false
     */
    fun existsByLicensePlateNumber(licensePlateNumber: String): Boolean
    
    /**
     * 번호판으로 차량을 삭제합니다.
     * 
     * @param licensePlateNumber 삭제할 차량의 번호판
     * @return 삭제된 레코드 수
     */
    fun deleteByLicensePlateNumber(licensePlateNumber: String): Long
}
