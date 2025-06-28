package com.example.hexagonal.parkinglot

/**
 * 주차장 이름을 나타내는 값 객체
 * 
 * 이 클래스는 주차장의 이름을 캡슐화하고 유효성을 검증합니다.
 * 도메인 주도 설계(DDD)의 값 객체(Value Object) 패턴을 따릅니다.
 */
data class ParkingLotName(val value: String) {
    
    init {
        require(value.isNotBlank()) { "주차장 이름은 비어있을 수 없습니다" }
    }
    
    override fun toString(): String = value
}
