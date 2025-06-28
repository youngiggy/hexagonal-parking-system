package com.example.hexagonal.car

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 차량 관련 비즈니스 로직을 처리하는 서비스
 * 
 * 이 서비스는 헥사고날 아키텍처에서 애플리케이션 서비스 계층에 해당하며,
 * 인바운드 포트(CarQueryUseCase, CarCommandUseCase)를 구현하고
 * 아웃바운드 포트(CarLoadPort, CarSavePort)를 사용하여 비즈니스 로직을 처리합니다.
 */
@Service
@Transactional
class CarService(
    private val savePort: CarSavePort,
    private val loadPort: CarLoadPort,
) : CarCommandUseCase, CarQueryUseCase {
    
    /**
     * 여러 차량을 일괄 생성합니다.
     * 
     * @param commands 생성할 차량들의 속성 정보
     * @return 생성된 차량 모델들
     */
    override fun bulkCreateCar(commands: Collection<CarProperties>): Collection<CarModel> {
        return savePort.saveAll(commands)
    }
    
    /**
     * 번호판으로 차량을 조회합니다.
     * 
     * @param licensePlateNumber 조회할 차량의 번호판
     * @return 조회된 차량 모델
     * @throws CarNotFoundException 해당 번호판의 차량이 존재하지 않는 경우
     */
    override fun getByLicensePlateNumber(licensePlateNumber: LicensePlateNumber): CarModel {
        return loadPort.findByLicensePlateNumberOrNull(licensePlateNumber)
            ?: throw CarNotFoundException("등록되지 않은 자동차입니다")
    }
}
