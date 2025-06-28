package com.example.hexagonal.parkinglot

/**
 * 이미 존재하는 주차장을 생성하려 할 때 발생하는 예외
 */
class ParkingLotAlreadyExistsException(message: String) : RuntimeException(message)
