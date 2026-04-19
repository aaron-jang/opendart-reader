package io.github.aaronjang.opendart.model

/**
 * 재무제표 구분 코드.
 * [OpenDartReader.finstateAll]의 `fsDiv` 파라미터에 사용합니다.
 */
enum class FinStatementType(val code: String, val description: String) {
    /** 연결재무제표 */
    CONSOLIDATED("CFS", "연결재무제표"),
    /** 별도(개별)재무제표 */
    SEPARATE("OFS", "별도(개별)재무제표"),
    ;

    override fun toString(): String = "$name($description)"
}
