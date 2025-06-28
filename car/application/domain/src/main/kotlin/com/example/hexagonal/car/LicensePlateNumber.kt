package com.example.hexagonal.car

@JvmInline
value class LicensePlateNumber(
    val value: String,
) {
    init {
        require(SPEC_LICENCE_PLATE_NUMBER.matches(value)) {
            "자동차 번호 형식이 유효하지 않습니다: $value"
        }
    }

    companion object {
        // 한국 자동차 번호판 정규식
        // [지역명(선택)] [숫자 1-3자리] [한글 1자리] [숫자 4자리]
        // 예: "서울 123 가 1234", "123 가 1234", "12가1234"
        val SPEC_LICENCE_PLATE_NUMBER = "[가-힣]{0,2}\\s?[0-9]{1,3}\\s?[가-힣]\\s?[0-9]{4}".toRegex()
    }
}
