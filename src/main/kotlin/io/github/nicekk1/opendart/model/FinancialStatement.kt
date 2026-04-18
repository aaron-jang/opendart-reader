package io.github.nicekk1.opendart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FinancialStatement(
    @SerialName("rcept_no") val rceptNo: String = "",
    @SerialName("corp_code") val corpCode: String = "",
    @SerialName("corp_name") val corpName: String = "",
    @SerialName("stock_code") val stockCode: String = "",
    @SerialName("reprt_code") val reprtCode: String = "",
    @SerialName("bsns_year") val bsnsYear: String = "",
    @SerialName("fs_div") val fsDiv: String = "",
    @SerialName("fs_nm") val fsNm: String = "",
    @SerialName("sj_div") val sjDiv: String = "",
    @SerialName("sj_nm") val sjNm: String = "",
    @SerialName("account_nm") val accountNm: String = "",
    @SerialName("thstrm_nm") val thstrmNm: String = "",
    @SerialName("thstrm_amount") val thstrmAmount: String = "",
    @SerialName("frmtrm_nm") val frmtrmNm: String = "",
    @SerialName("frmtrm_amount") val frmtrmAmount: String = "",
    @SerialName("bfefrmtrm_nm") val bfefrmtrmNm: String = "",
    @SerialName("bfefrmtrm_amount") val bfefrmtrmAmount: String = "",
    @SerialName("ord") val ord: String = "",
    @SerialName("currency") val currency: String = "",
)
