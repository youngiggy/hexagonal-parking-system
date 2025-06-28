package com.example.hexagonal.app

import com.example.hexagonal.car.*
import com.example.hexagonal.parkinglot.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 통합 주차 서비스
 * 
 * 이 클래스는 Car와 ParkingLot 도메인을 통합하여
 * 차량 등록부터 주차까지의 전체 플로우를 관리합니다.
 */
@Service
@Transactional
class IntegratedParkingService(
    private val carCommandUseCase: CarCommandUseCase,
    private val carQueryUseCase: CarQueryUseCase,
    private val parkingLotCommandUseCase: ParkingLotCommandUseCase,
    private val parkingLotQueryUseCase: ParkingLotQueryUseCase
) {
    
    /**
     * 차량 등록과 주차를 한 번에 처리합니다.
     */
    fun registerCarAndPark(
        licensePlateNumber: LicensePlateNumber,
        model: String,
        color: String,
        parkingLotName: ParkingLotName,
        totalSpaces: ParkingSpaceCount
    ): IntegratedParkingResult {
        
        // 1. 차량 등록 (Car 도메인의 현재 구조에 맞게 수정)
        val carData = CarData(licencePlateNumber = licensePlateNumber)
        val cars = carCommandUseCase.bulkCreateCar(listOf(carData))
        val car = cars.first()
        
        // 2. 주차장이 없으면 생성
        val parkingLot = try {
            parkingLotQueryUseCase.getParkingLotStatus(parkingLotName)
            // 주차장이 이미 존재함
            null
        } catch (e: ParkingLotNotFoundException) {
            // 주차장이 없으므로 생성
            parkingLotCommandUseCase.createParkingLot(parkingLotName, totalSpaces)
        }
        
        // 3. 차량 주차
        val parkingRecord = parkingLotCommandUseCase.parkCar(parkingLotName, licensePlateNumber)
        
        return IntegratedParkingResult(
            car = car,
            parkingRecord = parkingRecord,
            parkingLot = parkingLot
        )
    }
    
    /**
     * 차량 출차와 등록 해제를 한 번에 처리합니다.
     */
    fun leaveAndUnregisterCar(licensePlateNumber: LicensePlateNumber): IntegratedLeavingResult {
        
        // 1. 차량 출차
        val parkingRecord = parkingLotCommandUseCase.leaveCar(licensePlateNumber)
        
        // 2. 차량 조회 (현재 Car 도메인에는 삭제 기능이 없으므로 조회만)
        val car = carQueryUseCase.getByLicensePlateNumber(licensePlateNumber)
        
        return IntegratedLeavingResult(
            car = car,
            parkingRecord = parkingRecord
        )
    }
    
    /**
     * 특정 주차장의 등록된 차량 정보를 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getRegisteredCarsInParkingLot(parkingLotName: ParkingLotName): List<RegisteredCarInParkingLot> {
        val parkedCars = parkingLotQueryUseCase.getParkedCars(parkingLotName)
        
        return parkedCars.mapNotNull { parkingRecord ->
            try {
                val car = carQueryUseCase.getByLicensePlateNumber(parkingRecord.licensePlateNumber)
                RegisteredCarInParkingLot(
                    car = car,
                    parkingRecord = parkingRecord
                )
            } catch (e: Exception) {
                null // 등록되지 않은 차량은 제외
            }
        }
    }
}

/**
 * 통합 주차 결과
 */
data class IntegratedParkingResult(
    val car: CarModel,
    val parkingRecord: ParkingRecord,
    val parkingLot: ParkingLot? = null
)

/**
 * 통합 출차 결과
 */
data class IntegratedLeavingResult(
    val car: CarModel,
    val parkingRecord: ParkingRecord
)

/**
 * 주차장 내 등록된 차량 정보
 */
data class RegisteredCarInParkingLot(
    val car: CarModel,
    val parkingRecord: ParkingRecord
)
