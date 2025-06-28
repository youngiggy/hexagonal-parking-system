// ParkingLot JPA Adapter Module
plugins {
    id("org.springframework.boot") version "3.5.3" apply false
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.0.0"
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.3")
    }
}

dependencies {
    // 도메인 의존성
    implementation(project(":car:application:domain"))
    implementation(project(":car:application:port-out"))
    
    // Spring Boot & JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-context")
    
    // Database
    runtimeOnly("com.h2database:h2")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
