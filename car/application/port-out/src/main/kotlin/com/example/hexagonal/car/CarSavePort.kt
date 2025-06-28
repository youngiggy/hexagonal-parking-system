package com.example.hexagonal.car

/**
 * 차량 저장을 위한 아웃바운드 포트
 * 
 * 이 인터페이스는 차량 데이터를 외부 저장소(데이터베이스, 파일 시스템 등)에 
 * 저장하는 모든 기능을 정의합니다.
 * 헥사고날 아키텍처에서 아웃바운드 포트(Secondary Port)에 해당합니다.
 */
interface CarSavePort {
    
    /**
     * 여러 차량을 일괄 저장합니다.
     * 
     * @param cars 저장할 차량들의 속성 정보
     * @return 저장된 차량 모델들 (생성된 ID와 타임스탬프 포함)
     */
    fun saveAll(cars: Collection<CarProperties>): Collection<CarModel>
}
