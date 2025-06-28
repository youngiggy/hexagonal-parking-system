package com.example.hexagonal.car

/**
 * 차량 명령 유스케이스
 * 
 * 이 인터페이스는 차량 생성, 수정, 삭제와 관련된 모든 유스케이스를 정의합니다.
 * 헥사고날 아키텍처에서 인바운드 포트(Primary Port)에 해당합니다.
 */
interface CarCommandUseCase {
    
    /**
     * 여러 차량을 일괄 생성합니다.
     * 
     * @param commands 생성할 차량들의 속성 정보
     * @return 생성된 차량 모델들
     */
    fun bulkCreateCar(commands: Collection<CarProperties>): Collection<CarModel>
}
