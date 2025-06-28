package com.example.hexagonal.car

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 차량 정보에 대한 JPA 어댑터
 * 
 * 이 클래스는 헥사고날 아키텍처에서 Secondary Adapter에 해당하며,
 * 아웃바운드 포트(CarLoadPort, CarSavePort)를 구현하여
 * 실제 데이터베이스와의 연동을 담당합니다.
 */
@Component
@Transactional
class CarJpaAdapter(
    private val repository: CarJpaRepository
) : CarLoadPort, CarSavePort {
    
    /**
     * 번호판으로 차량을 조회합니다.
     * 
     * @param licensePlateNumber 조회할 차량의 번호판
     * @return 조회된 차량 모델, 존재하지 않으면 null
     */
    override fun findByLicensePlateNumberOrNull(licensePlateNumber: LicensePlateNumber): CarModel? {
        return repository.findByLicensePlateNumber(licensePlateNumber.value)?.toModel()
    }
    
    /**
     * 여러 차량을 일괄 저장합니다.
     * 
     * @param cars 저장할 차량들의 속성 정보
     * @return 저장된 차량 모델들 (생성된 ID와 타임스탬프 포함)
     */
    override fun saveAll(cars: Collection<CarProperties>): Collection<CarModel> {
        val entities = cars.map { carProperties -> CarJpaEntity(carProperties) }
        val savedEntities = repository.saveAll(entities)
        return savedEntities.map { entity -> entity.toModel() }
    }
}
