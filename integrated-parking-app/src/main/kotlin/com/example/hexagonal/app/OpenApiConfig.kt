package com.example.hexagonal.app

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI 3.0 설정
 * 
 * 이 설정은 Swagger UI를 통해 API 문서를 제공합니다.
 * 접근 경로: http://localhost:8080/swagger-ui.html
 */
@Configuration
class OpenApiConfig {
    
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("헥사고날 주차장 관리 시스템 API")
                    .description("""
                        ## 개요
                        헥사고날 아키텍처 패턴을 적용한 주차장 관리 시스템의 REST API입니다.
                        
                        ## 주요 기능
                        - 차량 등록 및 주차 통합 관리
                        - 차량 출차 및 등록 해제
                        - 주차장별 등록된 차량 조회
                        
                        ## 아키텍처
                        - **헥사고날 아키텍처**: 포트와 어댑터 패턴 적용
                        - **TDD**: 테스트 주도 개발 방법론 적용
                        - **멀티모듈**: Gradle 기반 모듈 분리
                        - **Spring Boot**: 의존성 주입 및 트랜잭션 관리
                        
                        ## 기술 스택
                        - Kotlin
                        - Spring Boot 3.5
                        - Spring Data JPA
                        - H2 Database
                        - Kotest (테스트)
                    """.trimIndent())
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("헥사고날 주차장 시스템 개발팀")
                            .email("dev@hexagonal-parking.com")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .addServersItem(
                Server()
                    .url("http://localhost:8080")
                    .description("개발 서버")
            )
            .addServersItem(
                Server()
                    .url("https://api.hexagonal-parking.com")
                    .description("운영 서버")
            )
    }
}
