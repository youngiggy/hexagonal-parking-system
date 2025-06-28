package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber

/**
 * 주차장 명령 유스케이스 인바운드 포트
 * 
 * 이 인터페이스는 주차장 상태 변경과 관련된 비즈니스 로직을 정의합니다.
 * 헥사고날 아키텍처에서 Primary Port 역할을 합니다.
 */
interface ParkingLotCommandUseCase {
    
    /**
     * 차량을 주차합니다.
     * 
     * @param parkingLotName 주차할 주차장 이름
     * @param licensePlateNumber 주차할 차량의 번호판
     * @return 주차 기록
     * @throws ParkingLotFullException 주차장이 만차인 경우
     * @throws CarAlreadyParkedException 이미 주차된 차량인 경우
     */
    fun parkCar(parkingLotName: ParkingLotName, licensePlateNumber: LicensePlateNumber): ParkingRecord
    
    /**
     * 차량을 출차합니다.
     * 
     * @param licensePlateNumber 출차할 차량의 번호판
     * @return 출차 기록
     * @throws CarNotParkedException 주차되지 않은 차량인 경우
     */
    fun leaveCar(licensePlateNumber: LicensePlateNumber): ParkingRecord
    
    /**
     * 새로운 주차장을 생성합니다.
     * 
     * @param name 주차장 이름
     * @param totalSpaces 총 주차 공간 수
     * @return 생성된 주차장
     * @throws ParkingLotAlreadyExistsException 이미 존재하는 주차장인 경우
     */
    fun createParkingLot(name: ParkingLotName, totalSpaces: ParkingSpaceCount): ParkingLot
}
