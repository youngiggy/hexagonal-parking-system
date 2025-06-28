// Integrated Parking Application Module
plugins {
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.0.0"
}

dependencies {
    // Car 도메인 의존성
    implementation(project(":car:application:domain"))
    implementation(project(":car:application:exception"))
    implementation(project(":car:application:port-in"))
    implementation(project(":car:application:port-out"))
    implementation(project(":car:application:service"))
    implementation(project(":car:adapter-jpa"))
    implementation(project(":car:adapter-rest"))
    
    // ParkingLot 어댑터 의존성
    implementation(project(":parking-lot-adapter-jpa"))
    implementation(project(":parking-lot-adapter-rest"))
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // API 문서화
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // JSON 처리
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Database
    runtimeOnly("com.h2database:h2")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
}
