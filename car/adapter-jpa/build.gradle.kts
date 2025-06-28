// Car JPA Adapter Module - 데이터베이스 연동
plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    kotlin("plugin.jpa") version "2.0.0"
}

dependencies {
    implementation(project(":car:application:domain"))
    implementation(project(":car:application:port-out"))
    
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.5.3")
    implementation("org.springframework:spring-context:6.1.14")
    
    // H2 Database for testing
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.3")
}
