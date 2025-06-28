package com.example.hexagonal.car

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

/**
 * 차량 정보를 데이터베이스에 저장하기 위한 JPA 엔티티
 * 
 * 이 클래스는 헥사고날 아키텍처에서 Secondary Adapter의 일부로,
 * 도메인 모델과 데이터베이스 테이블 간의 매핑을 담당합니다.
 */
@Entity
@Table(name = "cars")
class CarJpaEntity(
    /**
     * 자동차 번호판
     * 데이터베이스에서 유니크 제약조건을 가집니다.
     */
    @Column(name = "license_plate_number", nullable = false, unique = true)
    var licensePlateNumber: String = "",
    
    /**
     * 기본 키 (UUID)
     * 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID = UUID.randomUUID(),
    
    /**
     * 생성 시각
     * 엔티티가 처음 저장될 때 자동으로 설정됩니다.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    
    /**
     * 수정 시각
     * 엔티티가 업데이트될 때마다 자동으로 갱신됩니다.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    /**
     * CarProperties로부터 JPA 엔티티를 생성하는 생성자
     */
    constructor(carProperties: CarProperties) : this(
        licensePlateNumber = carProperties.licencePlateNumber.value
    )
    
    /**
     * JPA 엔티티를 도메인 모델로 변환합니다.
     * 
     * @return CarModel 도메인 모델
     */
    fun toModel(): CarModel = CarEntity(
        licencePlateNumber = LicensePlateNumber(licensePlateNumber),
        identity = CarKey(id),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
