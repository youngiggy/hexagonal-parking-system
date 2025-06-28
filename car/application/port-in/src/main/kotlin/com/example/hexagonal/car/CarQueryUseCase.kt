package com.example.hexagonal.car

/**
 * 차량 조회 유스케이스
 * 
 * 이 인터페이스는 차량 조회와 관련된 모든 유스케이스를 정의합니다.
 * 헥사고날 아키텍처에서 인바운드 포트(Primary Port)에 해당합니다.
 */
interface CarQueryUseCase {
    
    /**
     * 번호판으로 차량을 조회합니다.
     * 
     * @param licensePlateNumber 조회할 차량의 번호판
     * @return 조회된 차량 모델
     * @throws CarNotFoundException 해당 번호판의 차량이 존재하지 않는 경우
     */
    fun getByLicensePlateNumber(licensePlateNumber: LicensePlateNumber): CarModel
}
