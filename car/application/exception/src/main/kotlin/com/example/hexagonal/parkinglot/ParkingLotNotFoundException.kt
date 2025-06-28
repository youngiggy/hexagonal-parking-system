package com.example.hexagonal.parkinglot

/**
 * 주차장을 찾을 수 없을 때 발생하는 예외
 */
class ParkingLotNotFoundException(message: String) : RuntimeException(message)
