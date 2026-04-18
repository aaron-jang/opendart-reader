package io.github.nicekk1.opendart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Company(
    @SerialName("status") val status: String = "",
    @SerialName("message") val message: String = "",
    @SerialName("corp_code") val corpCode: String = "",
    @SerialName("corp_name") val corpName: String = "",
    @SerialName("corp_name_eng") val corpNameEng: String = "",
    @SerialName("stock_name") val stockName: String = "",
    @SerialName("stock_code") val stockCode: String = "",
    @SerialName("ceo_nm") val ceoNm: String = "",
    @SerialName("corp_cls") val corpCls: String = "",
    @SerialName("jurir_no") val jurirNo: String = "",
    @SerialName("bizr_no") val bizrNo: String = "",
    @SerialName("adres") val adres: String = "",
    @SerialName("hm_url") val hmUrl: String = "",
    @SerialName("ir_url") val irUrl: String = "",
    @SerialName("phn_no") val phnNo: String = "",
    @SerialName("fax_no") val faxNo: String = "",
    @SerialName("induty_code") val indutyCode: String = "",
    @SerialName("est_dt") val estDt: String = "",
    @SerialName("acc_mt") val accMt: String = "",
)
