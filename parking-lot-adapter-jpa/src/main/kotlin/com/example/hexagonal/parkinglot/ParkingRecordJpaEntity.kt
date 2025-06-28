package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import jakarta.persistence.*
import java.time.Instant

/**
 * 주차 기록 JPA 엔티티
 * 
 * 이 클래스는 주차 기록 정보를 데이터베이스에 저장하기 위한 JPA 엔티티입니다.
 * 헥사고날 아키텍처에서 인프라스트럭처 계층에 속합니다.
 */
@Entity
@Table(name = "parking_records")
data class ParkingRecordJpaEntity(
    @Column(name = "license_plate_number", nullable = false)
    val licensePlateNumber: String,
    
    @Column(name = "parking_lot_name", nullable = false)
    val parkingLotName: String,
    
    @Column(name = "parked_at", nullable = false)
    val parkedAt: Instant,
    
    @Column(name = "left_at")
    val leftAt: Instant? = null,
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) {
    
    /**
     * JPA 엔티티를 도메인 모델로 변환합니다.
     */
    fun toDomainModel(): ParkingRecord {
        return ParkingRecord(
            licensePlateNumber = LicensePlateNumber(licensePlateNumber),
            parkingLotName = ParkingLotName(parkingLotName),
            parkedAt = parkedAt,
            leftAt = leftAt
        )
    }
    
    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환합니다.
         */
        fun fromDomainModel(parkingRecord: ParkingRecord): ParkingRecordJpaEntity {
            return ParkingRecordJpaEntity(
                licensePlateNumber = parkingRecord.licensePlateNumber.value,
                parkingLotName = parkingRecord.parkingLotName.value,
                parkedAt = parkingRecord.parkedAt,
                leftAt = parkingRecord.leftAt
            )
        }
    }
}
