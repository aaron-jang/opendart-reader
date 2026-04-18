package io.github.nicekk1.opendart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Disclosure(
    @SerialName("corp_cls") val corpCls: String = "",
    @SerialName("corp_name") val corpName: String = "",
    @SerialName("corp_code") val corpCode: String = "",
    @SerialName("stock_code") val stockCode: String = "",
    @SerialName("rcept_no") val rceptNo: String = "",
    @SerialName("report_nm") val reportNm: String = "",
    @SerialName("rcept_dt") val rceptDt: String = "",
    @SerialName("flr_nm") val flrNm: String = "",
    @SerialName("rm") val rm: String = "",
)
