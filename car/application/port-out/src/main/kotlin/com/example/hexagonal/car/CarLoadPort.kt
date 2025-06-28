package com.example.hexagonal.car

/**
 * 차량 조회를 위한 아웃바운드 포트
 * 
 * 이 인터페이스는 차량 데이터를 외부 저장소(데이터베이스, 캐시 등)에서 
 * 조회하는 모든 기능을 정의합니다.
 * 헥사고날 아키텍처에서 아웃바운드 포트(Secondary Port)에 해당합니다.
 */
interface CarLoadPort {
    
    /**
     * 번호판으로 차량을 조회합니다.
     * 
     * @param licensePlateNumber 조회할 차량의 번호판
     * @return 조회된 차량 모델, 존재하지 않으면 null
     */
    fun findByLicensePlateNumberOrNull(licensePlateNumber: LicensePlateNumber): CarModel?
}
