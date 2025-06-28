package com.example.hexaparking.integration

import com.example.hexagonal.app.IntegratedParkingApplication
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestConstructor

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [IntegratedParkingApplication::class]
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class IntegratedParkingApiTest(
    @LocalServerPort private val port: Int,
    private val restTemplate: TestRestTemplate
) : StringSpec({
    
    val baseUrl = "http://localhost:$port"
    
    "애플리케이션 헬스 체크 테스트" {
        // when
        val response = restTemplate.getForEntity(
            "$baseUrl/actuator/health",
            String::class.java
        )
        
        // then
        response.statusCode shouldBe HttpStatus.OK
    }
    
    "통합 API 엔드포인트 존재 확인" {
        // when - 잘못된 요청으로 엔드포인트 존재 확인
        val response = restTemplate.postForEntity(
            "$baseUrl/api/integrated/register-and-park",
            emptyMap<String, Any>(),
            String::class.java
        )
        
        // then - 400 Bad Request (엔드포인트는 존재하지만 잘못된 요청)
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
    }
})
