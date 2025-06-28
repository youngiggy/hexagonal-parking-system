package com.example.hexagonal.car

/**
 * 차량을 찾을 수 없을 때 발생하는 예외
 * 
 * 이 예외는 다음과 같은 상황에서 발생합니다:
 * - 존재하지 않는 번호판으로 차량을 조회할 때
 * - 삭제된 차량에 접근하려고 할 때
 * - 잘못된 차량 ID로 조회할 때
 */
class CarNotFoundException : RuntimeException {
    
    /**
     * 메시지와 함께 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     */
    constructor(message: String?) : super(message)
    
    /**
     * 메시지와 원인 예외와 함께 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    
    /**
     * 원인 예외와 함께 예외를 생성합니다.
     * 
     * @param cause 원인 예외
     */
    constructor(cause: Throwable?) : super(cause)
}
