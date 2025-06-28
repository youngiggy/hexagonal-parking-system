package com.example.hexagonal.app

import com.example.hexagonal.car.LicensePlateNumber
import com.example.hexagonal.parkinglot.ParkingLotName
import com.example.hexagonal.parkinglot.ParkingSpaceCount
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

/**
 * 통합 주차 관리 REST API
 * 
 * 이 컨트롤러는 차량 등록부터 주차까지의 통합 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/integrated")
class IntegratedParkingRestApi(
    private val integratedParkingService: IntegratedParkingService
) {
    
    /**
     * 차량 등록과 주차를 한 번에 처리합니다.
     */
    @PostMapping("/register-and-park")
    fun registerAndPark(@Valid @RequestBody request: RegisterAndParkRequest): ResponseEntity<IntegratedParkingResponse> {
        return try {
            val result = integratedParkingService.registerCarAndPark(
                licensePlateNumber = LicensePlateNumber(request.licensePlateNumber),
                model = request.model,
                color = request.color,
                parkingLotName = ParkingLotName(request.parkingLotName),
                totalSpaces = ParkingSpaceCount(request.totalSpaces)
            )
            
            val response = IntegratedParkingResponse.fromDomainModel(result)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * 차량 출차와 등록 해제를 한 번에 처리합니다.
     */
    @PostMapping("/leave-and-unregister")
    fun leaveAndUnregister(@Valid @RequestBody request: LeaveAndUnregisterRequest): ResponseEntity<IntegratedLeavingResponse> {
        return try {
            val result = integratedParkingService.leaveAndUnregisterCar(
                licensePlateNumber = LicensePlateNumber(request.licensePlateNumber)
            )
            
            val response = IntegratedLeavingResponse.fromDomainModel(result)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
    
    /**
     * 특정 주차장의 등록된 차량 정보를 조회합니다.
     */
    @GetMapping("/parking-lots/{parkingLotName}/registered-cars")
    fun getRegisteredCarsInParkingLot(@PathVariable parkingLotName: String): ResponseEntity<List<RegisteredCarResponse>> {
        return try {
            val result = integratedParkingService.getRegisteredCarsInParkingLot(
                parkingLotName = ParkingLotName(parkingLotName)
            )
            
            val response = result.map { RegisteredCarResponse.fromDomainModel(it) }
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}

/**
 * 차량 등록 및 주차 요청 DTO
 */
data class RegisterAndParkRequest(
    @field:NotBlank(message = "번호판은 필수입니다")
    val licensePlateNumber: String,
    
    @field:NotBlank(message = "차량 모델은 필수입니다")
    val model: String,
    
    @field:NotBlank(message = "차량 색상은 필수입니다")
    val color: String,
    
    @field:NotBlank(message = "주차장 이름은 필수입니다")
    val parkingLotName: String,
    
    @field:Min(value = 1, message = "주차 공간은 1 이상이어야 합니다")
    val totalSpaces: Int
)

/**
 * 차량 출차 및 등록 해제 요청 DTO
 */
data class LeaveAndUnregisterRequest(
    @field:NotBlank(message = "번호판은 필수입니다")
    val licensePlateNumber: String
)

/**
 * 통합 주차 응답 DTO
 */
data class IntegratedParkingResponse(
    val licensePlateNumber: String,
    val parkingLotName: String,
    val parkedAt: Instant,
    val isParked: Boolean,
    val parkingLotCreated: Boolean
) {
    companion object {
        fun fromDomainModel(result: IntegratedParkingResult): IntegratedParkingResponse {
            return IntegratedParkingResponse(
                licensePlateNumber = result.car.licencePlateNumber.value,
                parkingLotName = result.parkingRecord.parkingLotName.value,
                parkedAt = result.parkingRecord.parkedAt,
                isParked = result.parkingRecord.isParked,
                parkingLotCreated = result.parkingLot != null
            )
        }
    }
}

/**
 * 통합 출차 응답 DTO
 */
data class IntegratedLeavingResponse(
    val licensePlateNumber: String,
    val leftAt: Instant?,
    val isParked: Boolean
) {
    companion object {
        fun fromDomainModel(result: IntegratedLeavingResult): IntegratedLeavingResponse {
            return IntegratedLeavingResponse(
                licensePlateNumber = result.car.licencePlateNumber.value,
                leftAt = result.parkingRecord.leftAt,
                isParked = result.parkingRecord.isParked
            )
        }
    }
}

/**
 * 등록된 차량 응답 DTO
 */
data class RegisteredCarResponse(
    val licensePlateNumber: String,
    val parkingLotName: String,
    val parkedAt: Instant,
    val carCreatedAt: Instant
) {
    companion object {
        fun fromDomainModel(registeredCar: RegisteredCarInParkingLot): RegisteredCarResponse {
            return RegisteredCarResponse(
                licensePlateNumber = registeredCar.car.licencePlateNumber.value,
                parkingLotName = registeredCar.parkingRecord.parkingLotName.value,
                parkedAt = registeredCar.parkingRecord.parkedAt,
                carCreatedAt = registeredCar.car.createdAt
            )
        }
    }
}
