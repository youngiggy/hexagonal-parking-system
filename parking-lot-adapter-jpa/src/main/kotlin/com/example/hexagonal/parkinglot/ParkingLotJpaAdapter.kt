package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 주차장 JPA 어댑터
 * 
 * 이 클래스는 주차장 도메인의 아웃바운드 포트를 구현하여
 * JPA를 통한 데이터베이스 접근을 제공합니다.
 * 헥사고날 아키텍처에서 Secondary Adapter 역할을 합니다.
 */
@Component
@Transactional
class ParkingLotJpaAdapter(
    private val parkingLotRepository: ParkingLotJpaRepository,
    private val parkingRecordRepository: ParkingRecordJpaRepository
) : ParkingLotLoadPort, ParkingLotSavePort {
    
    /**
     * 주차장을 저장합니다.
     */
    override fun saveParkingLot(parkingLot: ParkingLot): ParkingLot {
        val entity = ParkingLotJpaEntity.fromDomainModel(parkingLot)
        val savedEntity = parkingLotRepository.save(entity)
        return savedEntity.toDomainModel()
    }
    
    /**
     * 주차장을 조회합니다.
     */
    @Transactional(readOnly = true)
    override fun loadParkingLot(name: ParkingLotName): ParkingLot? {
        return parkingLotRepository.findByName(name.value)?.toDomainModel()
    }
    
    /**
     * 주차장 존재 여부를 확인합니다.
     */
    @Transactional(readOnly = true)
    override fun existsParkingLot(name: ParkingLotName): Boolean {
        return parkingLotRepository.existsByName(name.value)
    }
    
    /**
     * 주차장을 삭제합니다.
     */
    override fun deleteParkingLot(name: ParkingLotName): Boolean {
        return if (parkingLotRepository.existsByName(name.value)) {
            parkingLotRepository.deleteByName(name.value)
            true
        } else {
            false
        }
    }
    
    /**
     * 주차 기록을 저장합니다.
     */
    override fun saveParkingRecord(parkingRecord: ParkingRecord): ParkingRecord {
        val entity = ParkingRecordJpaEntity.fromDomainModel(parkingRecord)
        val savedEntity = parkingRecordRepository.save(entity)
        return savedEntity.toDomainModel()
    }
    
    /**
     * 주차 기록을 업데이트합니다.
     */
    override fun updateParkingRecord(parkingRecord: ParkingRecord): ParkingRecord {
        val entity = ParkingRecordJpaEntity.fromDomainModel(parkingRecord)
        val updatedEntity = parkingRecordRepository.save(entity)
        return updatedEntity.toDomainModel()
    }
    
    /**
     * 특정 차량의 주차 기록을 조회합니다.
     */
    @Transactional(readOnly = true)
    override fun loadParkingRecord(licensePlateNumber: LicensePlateNumber): ParkingRecord? {
        return parkingRecordRepository
            .findByLicensePlateNumberAndLeftAtIsNull(licensePlateNumber.value)
            ?.toDomainModel()
    }
    
    /**
     * 주차장에 현재 주차된 차량 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    override fun loadParkedCars(parkingLotName: ParkingLotName): List<ParkingRecord> {
        return parkingRecordRepository
            .findByParkingLotNameAndLeftAtIsNull(parkingLotName.value)
            .map { it.toDomainModel() }
    }
}
