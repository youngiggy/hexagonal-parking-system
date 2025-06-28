package com.example.hexagonal.parkinglot

/**
 * 주차장 상태 정보를 나타내는 값 객체
 * 
 * 이 클래스는 주차장의 현재 상태를 조회할 때 사용됩니다.
 * 도메인 주도 설계(DDD)의 값 객체(Value Object) 패턴을 따릅니다.
 */
data class ParkingLotStatus(
    val name: ParkingLotName,
    val totalSpaces: ParkingSpaceCount,
    val availableSpaces: ParkingSpaceCount,
    val occupiedSpaces: ParkingSpaceCount,
    val occupancyRate: Double
) {
    init {
        require(occupancyRate >= 0.0 && occupancyRate <= 1.0) { 
            "점유율은 0.0과 1.0 사이여야 합니다" 
        }
        require(totalSpaces.value == availableSpaces.value + occupiedSpaces.value) {
            "전체 공간 수는 사용 가능 공간 수와 점유 공간 수의 합과 같아야 합니다"
        }
    }
    
    /**
     * 주차장이 만차인지 확인합니다.
     */
    val isFull: Boolean
        get() = availableSpaces.value == 0
    
    /**
     * 주차장이 비어있는지 확인합니다.
     */
    val isEmpty: Boolean
        get() = occupiedSpaces.value == 0
    
    /**
     * 점유율을 백분율로 반환합니다.
     */
    fun getOccupancyPercentage(): Int {
        return (occupancyRate * 100).toInt()
    }
}
