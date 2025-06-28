package com.example.hexagonal.parkinglot

import com.example.hexagonal.car.LicensePlateNumber
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.Instant

/**
 * 주차장 생성 요청 DTO
 */
data class CreateParkingLotRequest(
    @field:NotBlank(message = "주차장 이름은 필수입니다")
    @JsonProperty("name")
    val name: String,
    
    @field:Min(value = 1, message = "총 주차 공간은 1 이상이어야 합니다")
    @JsonProperty("totalSpaces")
    val totalSpaces: Int
) {
    fun toParkingLotName(): ParkingLotName = ParkingLotName(name)
    fun toTotalSpaces(): ParkingSpaceCount = ParkingSpaceCount(totalSpaces)
}

/**
 * 주차장 응답 DTO
 */
data class ParkingLotResponse(
    @JsonProperty("name")
    val name: String,
    
    @JsonProperty("totalSpaces")
    val totalSpaces: Int,
    
    @JsonProperty("availableSpaces")
    val availableSpaces: Int,
    
    @JsonProperty("occupiedSpaces")
    val occupiedSpaces: Int,
    
    @JsonProperty("occupancyRate")
    val occupancyRate: Double
) {
    companion object {
        fun fromDomainModel(status: ParkingLotStatus): ParkingLotResponse {
            return ParkingLotResponse(
                name = status.name.value,
                totalSpaces = status.totalSpaces.value,
                availableSpaces = status.availableSpaces.value,
                occupiedSpaces = status.occupiedSpaces.value,
                occupancyRate = status.occupancyRate
            )
        }
    }
}

/**
 * 차량 주차 요청 DTO
 */
data class ParkCarRequest(
    @field:NotBlank(message = "번호판은 필수입니다")
    @JsonProperty("licensePlateNumber")
    val licensePlateNumber: String
) {
    fun toLicensePlateNumber(): LicensePlateNumber = LicensePlateNumber(licensePlateNumber)
}

/**
 * 차량 출차 요청 DTO
 */
data class LeaveCarRequest(
    @field:NotBlank(message = "번호판은 필수입니다")
    @JsonProperty("licensePlateNumber")
    val licensePlateNumber: String
) {
    fun toLicensePlateNumber(): LicensePlateNumber = LicensePlateNumber(licensePlateNumber)
}

/**
 * 주차 기록 응답 DTO
 */
data class ParkingRecordResponse(
    @JsonProperty("licensePlateNumber")
    val licensePlateNumber: String,
    
    @JsonProperty("parkingLotName")
    val parkingLotName: String,
    
    @JsonProperty("parkedAt")
    val parkedAt: Instant,
    
    @JsonProperty("leftAt")
    val leftAt: Instant?,
    
    @JsonProperty("isParked")
    val isParked: Boolean
) {
    companion object {
        fun fromDomainModel(parkingRecord: ParkingRecord): ParkingRecordResponse {
            return ParkingRecordResponse(
                licensePlateNumber = parkingRecord.licensePlateNumber.value,
                parkingLotName = parkingRecord.parkingLotName.value,
                parkedAt = parkingRecord.parkedAt,
                leftAt = parkingRecord.leftAt,
                isParked = parkingRecord.isParked
            )
        }
    }
}
