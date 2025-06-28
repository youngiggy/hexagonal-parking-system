package com.example.hexagonal.car

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * 차량 등록 요청을 위한 데이터 전송 객체
 * 
 * 이 클래스는 REST API를 통해 클라이언트로부터 받는 차량 정보를 담습니다.
 * 헥사고날 아키텍처에서 Primary Adapter의 일부로 사용됩니다.
 */
data class CarRequestDto(
    /**
     * 차량 번호판
     * 한국 번호판 형식을 따라야 합니다.
     */
    @field:NotBlank(message = "번호판은 필수입니다")
    @field:Pattern(
        regexp = "^[0-9]{1,3}[가-힣][0-9]{4}$",
        message = "올바른 번호판 형식이 아닙니다 (예: 123가1234)"
    )
    @JsonProperty("licensePlateNumber")
    val licensePlateNumber: String
) {
    /**
     * DTO를 도메인 속성 객체로 변환합니다.
     * 
     * @return CarProperties 도메인 속성 객체
     */
    fun toProperties(): CarProperties = CarData(
        licencePlateNumber = LicensePlateNumber(licensePlateNumber)
    )
}
