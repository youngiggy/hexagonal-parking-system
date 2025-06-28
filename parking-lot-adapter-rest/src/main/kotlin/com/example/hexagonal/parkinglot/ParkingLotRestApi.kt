package com.example.hexagonal.parkinglot

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 주차장 REST API 컨트롤러
 * 
 * 이 클래스는 주차장 관련 HTTP 요청을 처리하는 REST API를 제공합니다.
 * 헥사고날 아키텍처에서 Primary Adapter 역할을 합니다.
 */
@RestController
@RequestMapping("/api")
class ParkingLotRestApi(
    private val parkingLotQueryUseCase: ParkingLotQueryUseCase,
    private val parkingLotCommandUseCase: ParkingLotCommandUseCase
) {
    
    /**
     * 새로운 주차장을 생성합니다.
     */
    @PostMapping("/parking-lots")
    fun createParkingLot(@Valid @RequestBody request: CreateParkingLotRequest): ResponseEntity<ParkingLotResponse> {
        return try {
            val parkingLot = parkingLotCommandUseCase.createParkingLot(
                name = request.toParkingLotName(),
                totalSpaces = request.toTotalSpaces()
            )
            
            val status = parkingLot.getStatus()
            val response = ParkingLotResponse.fromDomainModel(status)
            
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: ParkingLotAlreadyExistsException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * 주차장 상태를 조회합니다.
     */
    @GetMapping("/parking-lots/{name}")
    fun getParkingLotStatus(@PathVariable name: String): ResponseEntity<ParkingLotResponse> {
        return try {
            val parkingLotName = ParkingLotName(name)
            val status = parkingLotQueryUseCase.getParkingLotStatus(parkingLotName)
            val response = ParkingLotResponse.fromDomainModel(status)
            
            ResponseEntity.ok(response)
        } catch (e: ParkingLotNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * 주차장에 현재 주차된 차량 목록을 조회합니다.
     */
    @GetMapping("/parking-lots/{name}/cars")
    fun getParkedCars(@PathVariable name: String): ResponseEntity<List<ParkingRecordResponse>> {
        return try {
            val parkingLotName = ParkingLotName(name)
            val parkedCars = parkingLotQueryUseCase.getParkedCars(parkingLotName)
            val response = parkedCars.map { ParkingRecordResponse.fromDomainModel(it) }
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * 차량을 주차합니다.
     */
    @PostMapping("/parking-lots/{name}/park")
    fun parkCar(
        @PathVariable name: String,
        @Valid @RequestBody request: ParkCarRequest
    ): ResponseEntity<ParkingRecordResponse> {
        return try {
            val parkingLotName = ParkingLotName(name)
            val licensePlateNumber = request.toLicensePlateNumber()
            
            val parkingRecord = parkingLotCommandUseCase.parkCar(parkingLotName, licensePlateNumber)
            val response = ParkingRecordResponse.fromDomainModel(parkingRecord)
            
            ResponseEntity.ok(response)
        } catch (e: ParkingLotNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        } catch (e: ParkingLotFullException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (e: CarAlreadyParkedException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * 차량을 출차합니다.
     */
    @PostMapping("/parking-lots/leave")
    fun leaveCar(@Valid @RequestBody request: LeaveCarRequest): ResponseEntity<ParkingRecordResponse> {
        return try {
            val licensePlateNumber = request.toLicensePlateNumber()
            val parkingRecord = parkingLotCommandUseCase.leaveCar(licensePlateNumber)
            val response = ParkingRecordResponse.fromDomainModel(parkingRecord)
            
            ResponseEntity.ok(response)
        } catch (e: CarNotParkedException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * 특정 차량의 주차 기록을 조회합니다.
     */
    @GetMapping("/parking-records/{licensePlateNumber}")
    fun getParkingRecord(@PathVariable licensePlateNumber: String): ResponseEntity<ParkingRecordResponse> {
        return try {
            val licenseNumber = com.example.hexagonal.car.LicensePlateNumber(licensePlateNumber)
            val parkingRecord = parkingLotQueryUseCase.findParkingRecord(licenseNumber)
            
            if (parkingRecord != null) {
                val response = ParkingRecordResponse.fromDomainModel(parkingRecord)
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * 주차장을 삭제합니다.
     */
    @DeleteMapping("/parking-lots/{name}")
    fun deleteParkingLot(@PathVariable name: String): ResponseEntity<Void> {
        return try {
            val parkingLotName = ParkingLotName(name)
            // 실제로는 ParkingLotCommandUseCase에 deleteParkingLot 메서드가 있어야 하지만
            // 현재는 구현되지 않았으므로 임시로 NO_CONTENT 반환
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
