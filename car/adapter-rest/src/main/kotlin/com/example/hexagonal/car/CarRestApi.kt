package com.example.hexagonal.car

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

/**
 * 차량 관련 REST API 컨트롤러
 * 
 * 이 클래스는 헥사고날 아키텍처에서 Primary Adapter에 해당하며,
 * HTTP 요청을 받아 인바운드 포트(UseCase)를 호출하여 비즈니스 로직을 처리합니다.
 */
@RestController
@RequestMapping("/api/cars")
class CarRestApi(
    private val queryUseCase: CarQueryUseCase,
    private val commandUseCase: CarCommandUseCase
) {
    
    /**
     * 여러 차량을 일괄 등록합니다.
     * 
     * @param requestDtos 등록할 차량들의 정보
     * @return 등록된 차량들의 정보
     */
    @PostMapping("/bulk")
    fun bulkCreateCars(
        @Valid @RequestBody requestDtos: List<CarRequestDto>
    ): ResponseEntity<List<CarResponseDto>> {
        val carProperties = requestDtos.map { it.toProperties() }
        val savedCars = commandUseCase.bulkCreateCar(carProperties)
        val responseDtos = savedCars.map { CarResponseDto.from(it) }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDtos)
    }
    
    /**
     * 번호판으로 차량을 조회합니다.
     * 
     * @param licensePlateNumber 조회할 차량의 번호판
     * @return 조회된 차량 정보
     */
    @GetMapping("/{licensePlateNumber}")
    fun getCarByLicensePlateNumber(
        @PathVariable licensePlateNumber: String
    ): ResponseEntity<CarResponseDto?> {
        return try {
            val licensePlate = LicensePlateNumber(licensePlateNumber)
            val car = queryUseCase.getByLicensePlateNumber(licensePlate)
            val responseDto = CarResponseDto.from(car)
            ResponseEntity.ok(responseDto)
        } catch (e: CarNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            // 잘못된 번호판 형식
            ResponseEntity.badRequest().build()
        }
    }
}
