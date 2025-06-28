package com.example.hexagonal.parkinglot

/**
 * 주차 공간 수를 나타내는 값 객체
 * 
 * 이 클래스는 주차장의 공간 수를 캡슐화하고 유효성을 검증합니다.
 * 도메인 주도 설계(DDD)의 값 객체(Value Object) 패턴을 따릅니다.
 */
data class ParkingSpaceCount(val value: Int) {
    
    init {
        require(value >= 0) { "주차 공간 수는 0 이상이어야 합니다" }
    }
    
    /**
     * 두 주차 공간 수를 더합니다.
     */
    operator fun plus(other: ParkingSpaceCount): ParkingSpaceCount {
        return ParkingSpaceCount(this.value + other.value)
    }
    
    /**
     * 두 주차 공간 수를 뺍니다.
     */
    operator fun minus(other: ParkingSpaceCount): ParkingSpaceCount {
        return ParkingSpaceCount(this.value - other.value)
    }
    
    /**
     * 다른 주차 공간 수보다 큰지 확인합니다.
     */
    fun isGreaterThan(other: ParkingSpaceCount): Boolean {
        return this.value > other.value
    }
    
    /**
     * 다른 주차 공간 수보다 작은지 확인합니다.
     */
    fun isLessThan(other: ParkingSpaceCount): Boolean {
        return this.value < other.value
    }
    
    override fun toString(): String = value.toString()
}
