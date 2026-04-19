package io.github.aaronjang.opendart.model

/**
 * 보고서 유형 코드.
 * [OpenDartReader.report], [OpenDartReader.finstate] 등의 `reprtCode` 파라미터에 사용합니다.
 */
enum class ReportType(val code: String, val description: String) {
    /** 사업보고서 (연간) */
    ANNUAL("11011", "사업보고서"),
    /** 반기보고서 */
    HALF_YEAR("11012", "반기보고서"),
    /** 1분기보고서 */
    Q1("11013", "1분기보고서"),
    /** 3분기보고서 */
    Q3("11014", "3분기보고서"),
    ;

    override fun toString(): String = "$name($description)"
}
