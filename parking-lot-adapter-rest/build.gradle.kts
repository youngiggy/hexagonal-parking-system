// ParkingLot REST Adapter Module
plugins {
    id("org.springframework.boot") version "3.5.3" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.3")
    }
}

dependencies {
    // 도메인 의존성
    implementation(project(":car:application:domain"))
    implementation(project(":car:application:exception"))
    implementation(project(":car:application:port-in"))
    
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // JSON 처리
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux") // WebTestClient
}
