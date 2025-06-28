package com.example.hexagonal.parkinglot

/**
 * 이미 주차된 차량을 다시 주차하려 할 때 발생하는 예외
 */
class CarAlreadyParkedException(message: String) : RuntimeException(message)
