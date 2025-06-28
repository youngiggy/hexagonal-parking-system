package com.example.hexagonal.car

import java.time.Instant
import java.util.UUID

/**
 * 차량의 기본 속성을 정의하는 인터페이스
 */
interface CarProperties {
    val licencePlateNumber: LicensePlateNumber
    // 차종, 소유자 등등의 속성은 생략한다
}

/**
 * 차량의 고유 식별자를 정의하는 인터페이스
 */
interface CarIdentity {
    val value: UUID
}

/**
 * 완전한 차량 모델을 정의하는 인터페이스
 * CarProperties와 CarIdentity를 상속하여 모든 차량 정보를 포함
 */
interface CarModel : CarProperties {
    val identity: CarIdentity
    val createdAt: Instant
    val updatedAt: Instant
}

/**
 * 차량 생성/수정 시 사용되는 데이터 클래스
 */
data class CarData(
    override val licencePlateNumber: LicensePlateNumber,
) : CarProperties

/**
 * 차량의 고유 식별자를 나타내는 값 객체
 */
data class CarKey(
    override val value: UUID,
) : CarIdentity

/**
 * 완전한 차량 엔티티
 * 모든 차량 정보를 포함하는 도메인 엔티티
 */
data class CarEntity(
    override val licencePlateNumber: LicensePlateNumber,
    override val identity: CarIdentity,
    override val createdAt: Instant,
    override val updatedAt: Instant,
) : CarModel
