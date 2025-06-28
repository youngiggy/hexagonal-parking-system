package com.example.hexagonal.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * 통합 주차 관리 애플리케이션
 * 
 * 이 애플리케이션은 Car와 ParkingLot 도메인을 통합하여
 * 완전한 주차 관리 시스템을 제공합니다.
 */
@SpringBootApplication(scanBasePackages = ["com.example.hexagonal"])
@EntityScan(basePackages = ["com.example.hexagonal"])
@EnableJpaRepositories(basePackages = ["com.example.hexagonal"])
class IntegratedParkingApplication

fun main(args: Array<String>) {
    runApplication<IntegratedParkingApplication>(*args)
}
