package com.example.hexagonal.app

import com.example.hexagonal.car.LicensePlateNumber
import com.example.hexagonal.parkinglot.ParkingLotName
import com.example.hexagonal.parkinglot.ParkingSpaceCount
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "통합 주차 관리", description = "차량 등록과 주차를 통합 관리하는 API")
class IntegratedParkingRestApi(
    private val integratedParkingService: IntegratedParkingService
) {
    
    /**
     * 차량 등록과 주차를 한 번에 처리합니다.
     */
    @PostMapping("/register-and-park")
    @Operation(
        summary = "차량 등록 및 주차",
        description = "새로운 차량을 등록하고 지정된 주차장에 주차시킵니다. 주차장이 존재하지 않으면 새로 생성합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "차량 등록 및 주차 성공",
                content = [Content(schema = Schema(implementation = IntegratedParkingResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(implementation = String::class))]
            )
        ]
    )
    fun registerAndPark(
        @Valid @RequestBody 
        @Parameter(description = "차량 등록 및 주차 요청 정보")
        request: RegisterAndParkRequest
    ): ResponseEntity<IntegratedParkingResponse> {
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
    @Operation(
        summary = "차량 출차 및 등록 해제",
        description = "주차된 차량을 출차시키고 시스템에서 등록을 해제합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "차량 출차 및 등록 해제 성공",
                content = [Content(schema = Schema(implementation = IntegratedLeavingResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "차량을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(implementation = String::class))]
            )
        ]
    )
    fun leaveAndUnregister(
        @Valid @RequestBody 
        @Parameter(description = "차량 출차 및 등록 해제 요청 정보")
        request: LeaveAndUnregisterRequest
    ): ResponseEntity<IntegratedLeavingResponse> {
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
    @Operation(
        summary = "주차장별 등록된 차량 조회",
        description = "지정된 주차장에 현재 주차되어 있는 모든 등록된 차량의 정보를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "등록된 차량 목록 조회 성공",
                content = [Content(schema = Schema(implementation = Array<RegisteredCarResponse>::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(implementation = String::class))]
            )
        ]
    )
    fun getRegisteredCarsInParkingLot(
        @PathVariable 
        @Parameter(description = "주차장 이름", example = "강남주차장")
        parkingLotName: String
    ): ResponseEntity<List<RegisteredCarResponse>> {
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
@Schema(description = "차량 등록 및 주차 요청")
data class RegisterAndParkRequest(
    @field:NotBlank(message = "번호판은 필수입니다")
    @Schema(description = "차량 번호판", example = "서울 123 가 1234", required = true)
    val licensePlateNumber: String,
    
    @field:NotBlank(message = "차량 모델은 필수입니다")
    @Schema(description = "차량 모델", example = "소나타", required = true)
    val model: String,
    
    @field:NotBlank(message = "차량 색상은 필수입니다")
    @Schema(description = "차량 색상", example = "흰색", required = true)
    val color: String,
    
    @field:NotBlank(message = "주차장 이름은 필수입니다")
    @Schema(description = "주차장 이름", example = "강남주차장", required = true)
    val parkingLotName: String,
    
    @field:Min(value = 1, message = "주차 공간은 1 이상이어야 합니다")
    @Schema(description = "주차장 총 공간 수", example = "100", minimum = "1", required = true)
    val totalSpaces: Int
)

/**
 * 차량 출차 및 등록 해제 요청 DTO
 */
@Schema(description = "차량 출차 및 등록 해제 요청")
data class LeaveAndUnregisterRequest(
    @field:NotBlank(message = "번호판은 필수입니다")
    @Schema(description = "차량 번호판", example = "서울 123 가 1234", required = true)
    val licensePlateNumber: String
)

/**
 * 통합 주차 응답 DTO
 */
@Schema(description = "차량 등록 및 주차 응답")
data class IntegratedParkingResponse(
    @Schema(description = "차량 번호판", example = "서울 123 가 1234")
    val licensePlateNumber: String,
    
    @Schema(description = "주차장 이름", example = "강남주차장")
    val parkingLotName: String,
    
    @Schema(description = "주차 시간", example = "2025-06-28T12:00:00Z")
    val parkedAt: Instant,
    
    @Schema(description = "주차 상태", example = "true")
    val isParked: Boolean,
    
    @Schema(description = "주차장 신규 생성 여부", example = "true")
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
@Schema(description = "차량 출차 및 등록 해제 응답")
data class IntegratedLeavingResponse(
    @Schema(description = "차량 번호판", example = "서울 123 가 1234")
    val licensePlateNumber: String,
    
    @Schema(description = "출차 시간", example = "2025-06-28T15:00:00Z")
    val leftAt: Instant?,
    
    @Schema(description = "주차 상태", example = "false")
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
@Schema(description = "등록된 차량 정보")
data class RegisteredCarResponse(
    @Schema(description = "차량 번호판", example = "서울 123 가 1234")
    val licensePlateNumber: String,
    
    @Schema(description = "주차장 이름", example = "강남주차장")
    val parkingLotName: String,
    
    @Schema(description = "주차 시간", example = "2025-06-28T12:00:00Z")
    val parkedAt: Instant,
    
    @Schema(description = "차량 등록 시간", example = "2025-06-28T11:30:00Z")
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
