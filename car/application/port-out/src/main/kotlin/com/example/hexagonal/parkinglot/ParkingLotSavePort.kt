package com.example.hexagonal.parkinglot

/**
 * 주차장 저장 아웃바운드 포트
 * 
 * 이 인터페이스는 주차장 데이터 저장을 위한 외부 시스템과의 연동을 정의합니다.
 * 헥사고날 아키텍처에서 Secondary Port 역할을 합니다.
 */
interface ParkingLotSavePort {
    
    /**
     * 주차장을 저장합니다.
     * 
     * @param parkingLot 저장할 주차장
     * @return 저장된 주차장
     */
    fun saveParkingLot(parkingLot: ParkingLot): ParkingLot
    
    /**
     * 주차 기록을 저장합니다.
     * 
     * @param parkingRecord 저장할 주차 기록
     * @return 저장된 주차 기록
     */
    fun saveParkingRecord(parkingRecord: ParkingRecord): ParkingRecord
    
    /**
     * 주차 기록을 업데이트합니다.
     * 
     * @param parkingRecord 업데이트할 주차 기록
     * @return 업데이트된 주차 기록
     */
    fun updateParkingRecord(parkingRecord: ParkingRecord): ParkingRecord
    
    /**
     * 주차장을 삭제합니다.
     * 
     * @param name 삭제할 주차장 이름
     * @return 삭제 성공 여부
     */
    fun deleteParkingLot(name: ParkingLotName): Boolean
}
