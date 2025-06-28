package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber

/**
 * 주차장 조회 유스케이스 인바운드 포트
 * 
 * 이 인터페이스는 주차장 상태 조회와 관련된 비즈니스 로직을 정의합니다.
 * 헥사고날 아키텍처에서 Primary Port 역할을 합니다.
 */
interface ParkingLotQueryUseCase {
    
    /**
     * 주차장 상태를 조회합니다.
     * 
     * @param name 조회할 주차장 이름
     * @return 주차장 상태 정보
     */
    fun getParkingLotStatus(name: ParkingLotName): ParkingLotStatus
    
    /**
     * 주차장에 현재 주차된 차량 목록을 조회합니다.
     * 
     * @param name 조회할 주차장 이름
     * @return 주차된 차량들의 주차 기록 목록
     */
    fun getParkedCars(name: ParkingLotName): List<ParkingRecord>
    
    /**
     * 특정 차량의 주차 기록을 조회합니다.
     * 
     * @param licensePlateNumber 조회할 차량의 번호판
     * @return 주차 기록 (주차되지 않은 경우 null)
     */
    fun findParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord?
}
