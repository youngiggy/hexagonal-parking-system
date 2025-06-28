// Car REST Adapter Module - HTTP API 제공
plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":car:application:domain"))
    implementation(project(":car:application:port-in"))
    
    implementation("org.springframework.boot:spring-boot-starter-web:3.5.3")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.5.3")
    implementation("org.springframework:spring-context:6.1.14")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.3")
}
