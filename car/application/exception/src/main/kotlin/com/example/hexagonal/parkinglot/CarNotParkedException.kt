package com.example.hexagonal.parkinglot

/**
 * 주차되지 않은 차량을 출차하려 할 때 발생하는 예외
 */
class CarNotParkedException(message: String) : RuntimeException(message)
