package com.example.hexagonal.parkinglot

import jakarta.persistence.*

/**
 * 주차장 JPA 엔티티
 * 
 * 이 클래스는 주차장 정보를 데이터베이스에 저장하기 위한 JPA 엔티티입니다.
 * 헥사고날 아키텍처에서 인프라스트럭처 계층에 속합니다.
 */
@Entity
@Table(name = "parking_lots")
data class ParkingLotJpaEntity(
    @Column(name = "name", nullable = false, unique = true)
    val name: String,
    
    @Column(name = "total_spaces", nullable = false)
    val totalSpaces: Int,
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) {
    
    /**
     * JPA 엔티티를 도메인 모델로 변환합니다.
     */
    fun toDomainModel(): ParkingLot {
        return ParkingLot(
            name = ParkingLotName(name),
            totalSpaces = ParkingSpaceCount(totalSpaces)
        )
    }
    
    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환합니다.
         */
        fun fromDomainModel(parkingLot: ParkingLot): ParkingLotJpaEntity {
            return ParkingLotJpaEntity(
                name = parkingLot.name.value,
                totalSpaces = parkingLot.totalSpaces.value
            )
        }
    }
}
