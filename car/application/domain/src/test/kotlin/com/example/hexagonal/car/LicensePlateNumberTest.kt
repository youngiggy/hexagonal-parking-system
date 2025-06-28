package com.example.hexagonal.car

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class LicensePlateNumberTest :
    StringSpec({
        "유효한 번호판 형식이면 생성된다" {
            // given
            val validPlateNumbers =
                listOf(
                    "서울 123 가 1234",
                    "경기 12 나 3456",
                    "부산 1 다 7890",
                    "123 가 1234", // 지역명 없는 경우
                    "12가1234", // 공백 없는 경우
                    "서울123가1234", // 모든 공백 없는 경우
                    "1가1234", // 최소 숫자
                )

            // when & then
            validPlateNumbers.forEach { plateNumber ->
                shouldNotThrow<IllegalArgumentException> {
                    LicensePlateNumber(plateNumber)
                }
            }
        }

        "잘못된 번호판 형식이면 예외가 발생한다" {
            // given
            val invalidPlateNumbers =
                listOf(
                    "invalid",
                    "123456",
                    "abc def",
                    "",
                    "서울 abc 가 1234", // 숫자 부분에 문자
                    "서울 123 가 abc", // 마지막 숫자 부분에 문자
                    "서울 1234 가 1234", // 첫 번째 숫자가 4자리 (최대 3자리)
                    "서울 123 가가 1234", // 한글이 2자리
                    "서울 123 가 12345", // 마지막 숫자가 5자리 (정확히 4자리여야 함)
                )

            // when & then
            invalidPlateNumbers.forEach { plateNumber ->
                shouldThrow<IllegalArgumentException> {
                    LicensePlateNumber(plateNumber)
                }
            }
        }

        "번호판 값이 올바르게 저장된다" {
            // given
            val plateNumber = "서울 123 가 1234"

            // when
            val licensePlateNumber = LicensePlateNumber(plateNumber)

            // then
            licensePlateNumber.value shouldBe plateNumber
        }

        "동일한 번호판은 같다고 판단된다" {
            // given
            val plateNumber = "서울 123 가 1234"
            val licensePlateNumber1 = LicensePlateNumber(plateNumber)
            val licensePlateNumber2 = LicensePlateNumber(plateNumber)

            // when & then
            licensePlateNumber1 shouldBe licensePlateNumber2
        }

        "예외 메시지에 잘못된 번호판 정보가 포함된다" {
            // given
            val invalidPlateNumber = "invalid-plate"

            // when
            val exception =
                shouldThrow<IllegalArgumentException> {
                    LicensePlateNumber(invalidPlateNumber)
                }

            // then
            exception.message shouldContain invalidPlateNumber
            exception.message shouldContain "자동차 번호 형식이 유효하지 않습니다"
        }
    })
