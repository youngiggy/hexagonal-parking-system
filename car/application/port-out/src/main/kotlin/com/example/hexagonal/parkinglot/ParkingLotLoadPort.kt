package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber

/**
 * 주차장 조회 아웃바운드 포트
 * 
 * 이 인터페이스는 주차장 데이터 조회를 위한 외부 시스템과의 연동을 정의합니다.
 * 헥사고날 아키텍처에서 Secondary Port 역할을 합니다.
 */
interface ParkingLotLoadPort {
    
    /**
     * 주차장을 조회합니다.
     * 
     * @param name 조회할 주차장 이름
     * @return 주차장 (존재하지 않는 경우 null)
     */
    fun loadParkingLot(name: ParkingLotName): ParkingLot?
    
    /**
     * 특정 차량의 주차 기록을 조회합니다.
     * 
     * @param licensePlateNumber 조회할 차량의 번호판
     * @return 주차 기록 (존재하지 않는 경우 null)
     */
    fun loadParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord?
    
    /**
     * 주차장에 현재 주차된 차량 목록을 조회합니다.
     * 
     * @param parkingLotName 조회할 주차장 이름
     * @return 주차된 차량들의 주차 기록 목록
     */
    fun loadParkedCars(parkingLotName: ParkingLotName): List<ParkingRecord>
    
    /**
     * 주차장이 존재하는지 확인합니다.
     * 
     * @param name 확인할 주차장 이름
     * @return 존재 여부
     */
    fun existsParkingLot(name: ParkingLotName): Boolean
}
