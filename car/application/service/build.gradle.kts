// Car Service Module - 유스케이스 구현
plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":car:application:domain"))
    implementation(project(":car:application:exception"))
    implementation(project(":car:application:port-in"))
    implementation(project(":car:application:port-out"))
    
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
}
