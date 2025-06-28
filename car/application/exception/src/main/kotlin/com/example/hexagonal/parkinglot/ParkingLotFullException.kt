package com.example.hexagonal.parkinglot

/**
 * 주차장이 만차일 때 발생하는 예외
 */
class ParkingLotFullException(message: String) : RuntimeException(message)
