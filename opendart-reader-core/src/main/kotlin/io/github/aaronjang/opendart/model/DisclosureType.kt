package io.github.aaronjang.opendart.model

/**
 * 공시 유형 코드.
 * [OpenDartReader.list]의 `kind` 파라미터에 사용합니다.
 */
enum class DisclosureType(val code: String, val description: String) {
    /** 정기공시 */
    REGULAR("A", "정기공시"),
    /** 주요사항보고 */
    MAJOR_REPORT("B", "주요사항보고"),
    /** 발행공시 */
    ISSUANCE("C", "발행공시"),
    /** 지분공시 */
    EQUITY("D", "지분공시"),
    /** 기타공시 */
    OTHER("E", "기타공시"),
    /** 외부감사관련 */
    EXTERNAL_AUDIT("F", "외부감사관련"),
    /** 펀드공시 */
    FUND("G", "펀드공시"),
    /** 자산유동화 */
    ASSET_SECURITIZATION("H", "자산유동화"),
    /** 거래소공시 */
    EXCHANGE("I", "거래소공시"),
    /** 공정위공시 */
    FAIR_TRADE("J", "공정위공시"),
    ;

    override fun toString(): String = "$name($description)"
}
