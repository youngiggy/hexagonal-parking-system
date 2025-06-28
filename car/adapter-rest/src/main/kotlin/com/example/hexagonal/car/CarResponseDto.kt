package com.example.hexagonal.car

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

/**
 * 차량 정보 응답을 위한 데이터 전송 객체
 * 
 * 이 클래스는 REST API를 통해 클라이언트에게 반환하는 차량 정보를 담습니다.
 * 헥사고날 아키텍처에서 Primary Adapter의 일부로 사용됩니다.
 */
data class CarResponseDto(
    /**
     * 차량 고유 식별자
     */
    @JsonProperty("id")
    val id: UUID,
    
    /**
     * 차량 번호판
     */
    @JsonProperty("licensePlateNumber")
    val licensePlateNumber: String,
    
    /**
     * 생성 시각
     */
    @JsonProperty("createdAt")
    val createdAt: Instant,
    
    /**
     * 수정 시각
     */
    @JsonProperty("updatedAt")
    val updatedAt: Instant
) {
    companion object {
        /**
         * 도메인 모델로부터 응답 DTO를 생성합니다.
         * 
         * @param carModel 차량 도메인 모델
         * @return CarResponseDto 응답 DTO
         */
        fun from(carModel: CarModel): CarResponseDto = CarResponseDto(
            id = carModel.identity.value,
            licensePlateNumber = carModel.licencePlateNumber.value,
            createdAt = carModel.createdAt,
            updatedAt = carModel.updatedAt
        )
    }
}
