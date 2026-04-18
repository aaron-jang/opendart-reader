# OpenDartReaderKt Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port the Python OpenDartReader library to a Kotlin/JVM library with identical functionality.

**Architecture:** Facade pattern - `OpenDartReader` class delegates to internal modules for each API domain. Ktor Client for HTTP, kotlinx.serialization for JSON, Jsoup for HTML scraping. All network calls are `suspend fun`.

**Tech Stack:** Kotlin/JVM 17, Gradle KTS, Ktor Client (CIO), kotlinx.serialization, Jsoup, kotlin.test + JUnit5

---

## File Structure

```
build.gradle.kts                                    — Build config, dependencies, publishing
settings.gradle.kts                                  — Project name
gradle.properties                                    — JVM/Kotlin settings
src/main/kotlin/io/github/nicekk1/opendart/
├── OpenDartReader.kt                                — Main facade class
├── model/
│   ├── CorpCode.kt                                  — Corp code data class
│   ├── Disclosure.kt                                — Disclosure list data class
│   ├── Company.kt                                   — Company info data class
│   ├── FinancialStatement.kt                        — Financial statement data class
│   └── SubDocument.kt                               — Sub-document / attachment data class
├── internal/
│   ├── DartClient.kt                                — HTTP client wrapper (Ktor)
│   ├── CorpCodeCache.kt                             — Corp code daily cache
│   ├── KeywordMaps.kt                               — All keyword→endpoint mappings
│   └── DateUtils.kt                                 — Date parsing utilities
├── api/
│   ├── DartList.kt                                  — Disclosure list, company, document APIs
│   ├── DartReport.kt                                — Business report API (28 keywords)
│   ├── DartFinState.kt                              — Financial statement APIs
│   ├── DartShare.kt                                 — Shareholder disclosure APIs
│   ├── DartEvent.kt                                 — Corporate event API (36 keywords)
│   └── DartRegState.kt                              — Securities registration API (6 keywords)
├── scraping/
│   └── DartScraper.kt                               — Web scraping (list_date_ex, sub_docs, etc.)
└── exception/
    └── DartException.kt                             — API error exception
src/test/kotlin/io/github/nicekk1/opendart/
├── internal/
│   ├── KeywordMapsTest.kt                           — Keyword mapping tests
│   ├── DateUtilsTest.kt                             — Date parsing tests
│   └── CorpCodeCacheTest.kt                         — Corp code XML parsing test
├── api/
│   ├── DartListTest.kt                              — List API tests with MockEngine
│   ├── DartReportTest.kt                            — Report API test
│   ├── DartFinStateTest.kt                          — Finstate API tests
│   ├── DartShareTest.kt                             — Share API test
│   ├── DartEventTest.kt                             — Event API test
│   └── DartRegStateTest.kt                          — Regstate API test
├── scraping/
│   └── DartScraperTest.kt                           — Scraping tests
└── OpenDartReaderTest.kt                            — Facade integration test
```

---

### Task 1: Gradle Project Setup

**Files:**
- Create: `build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `src/main/kotlin/io/github/nicekk1/opendart/.gitkeep`
- Create: `src/test/kotlin/io/github/nicekk1/opendart/.gitkeep`

- [ ] **Step 1: Create settings.gradle.kts**

```kotlin
// settings.gradle.kts
rootProject.name = "opendart-reader-kt"
```

- [ ] **Step 2: Create gradle.properties**

```properties
kotlin.code.style=official
org.gradle.jvmargs=-Xmx1024m
```

- [ ] **Step 3: Create build.gradle.kts**

```kotlin
plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    `maven-publish`
}

group = "io.github.nicekk1"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion = "3.1.3"

dependencies {
    // Ktor Client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // HTML parsing
    implementation("org.jsoup:jsoup:1.18.3")

    // Test
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
```

- [ ] **Step 4: Initialize Gradle wrapper**

Run: `gradle wrapper --gradle-version 8.14`
Expected: `gradle/wrapper/` directory created, `gradlew` script created

- [ ] **Step 5: Create source directories**

Run:
```bash
mkdir -p src/main/kotlin/io/github/nicekk1/opendart/{model,internal,api,scraping,exception}
mkdir -p src/test/kotlin/io/github/nicekk1/opendart/{internal,api,scraping}
```

- [ ] **Step 6: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add build.gradle.kts settings.gradle.kts gradle.properties gradlew gradlew.bat gradle/ src/ .gitignore
git commit -m "feat: initialize Gradle project with Ktor and kotlinx.serialization"
```

---

### Task 2: Exception and Date Utilities

**Files:**
- Create: `src/main/kotlin/io/github/nicekk1/opendart/exception/DartException.kt`
- Create: `src/main/kotlin/io/github/nicekk1/opendart/internal/DateUtils.kt`
- Create: `src/test/kotlin/io/github/nicekk1/opendart/internal/DateUtilsTest.kt`

- [ ] **Step 1: Write DateUtils tests**

```kotlin
// src/test/kotlin/io/github/nicekk1/opendart/internal/DateUtilsTest.kt
package io.github.nicekk1.opendart.internal

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateUtilsTest {

    @Test
    fun `parseDate with LocalDate returns same date`() {
        val date = LocalDate.of(2024, 1, 15)
        assertEquals(date, parseDate(date))
    }

    @Test
    fun `parseDate with null returns null`() {
        assertEquals(null, parseDate(null))
    }

    @Test
    fun `formatDate formats as yyyyMMdd`() {
        val date = LocalDate.of(2024, 3, 5)
        assertEquals("20240305", formatDate(date))
    }

    @Test
    fun `defaultStart returns 1900-01-01`() {
        assertEquals(LocalDate.of(1900, 1, 1), defaultStart())
    }

    @Test
    fun `defaultEnd returns today`() {
        assertEquals(LocalDate.now(), defaultEnd())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.internal.DateUtilsTest"`
Expected: FAIL - unresolved reference

- [ ] **Step 3: Write DartException**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/exception/DartException.kt
package io.github.nicekk1.opendart.exception

class DartException(
    val status: String,
    override val message: String
) : RuntimeException("[$status] $message")
```

- [ ] **Step 4: Write DateUtils**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/internal/DateUtils.kt
package io.github.nicekk1.opendart.internal

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FORMAT_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd")

fun parseDate(date: Any?): LocalDate? = when (date) {
    null -> null
    is LocalDate -> date
    else -> throw IllegalArgumentException("Unsupported date type: ${date::class}")
}

fun formatDate(date: LocalDate): String = date.format(FORMAT_YYYYMMDD)

fun defaultStart(): LocalDate = LocalDate.of(1900, 1, 1)

fun defaultEnd(): LocalDate = LocalDate.now()
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.internal.DateUtilsTest"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/io/github/nicekk1/opendart/exception/DartException.kt \
        src/main/kotlin/io/github/nicekk1/opendart/internal/DateUtils.kt \
        src/test/kotlin/io/github/nicekk1/opendart/internal/DateUtilsTest.kt
git commit -m "feat: add DartException and DateUtils"
```

---

### Task 3: Keyword Mappings

**Files:**
- Create: `src/main/kotlin/io/github/nicekk1/opendart/internal/KeywordMaps.kt`
- Create: `src/test/kotlin/io/github/nicekk1/opendart/internal/KeywordMapsTest.kt`

- [ ] **Step 1: Write keyword mapping tests**

```kotlin
// src/test/kotlin/io/github/nicekk1/opendart/internal/KeywordMapsTest.kt
package io.github.nicekk1.opendart.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

class KeywordMapsTest {

    @Test
    fun `report keyword map has 28 entries`() {
        assertEquals(28, REPORT_KEYWORD_MAP.size)
    }

    @Test
    fun `report keyword map contains expected entries`() {
        assertEquals("alotMatter", REPORT_KEYWORD_MAP["배당"])
        assertEquals("empSttus", REPORT_KEYWORD_MAP["직원"])
        assertEquals("tesstkAcqsDspsSttus", REPORT_KEYWORD_MAP["자기주식"])
    }

    @Test
    fun `event keyword map has 36 entries`() {
        assertEquals(36, EVENT_KEYWORD_MAP.size)
    }

    @Test
    fun `event keyword map contains expected entries`() {
        assertEquals("piicDecsn", EVENT_KEYWORD_MAP["유상증자"])
        assertEquals("cmpMgDecsn", EVENT_KEYWORD_MAP["회사합병"])
    }

    @Test
    fun `regstate keyword map has 6 entries`() {
        assertEquals(6, REGSTATE_KEYWORD_MAP.size)
    }

    @Test
    fun `regstate keyword map contains expected entries`() {
        assertEquals("mgRs", REGSTATE_KEYWORD_MAP["합병"])
        assertEquals("bdRs", REGSTATE_KEYWORD_MAP["채무증권"])
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.internal.KeywordMapsTest"`
Expected: FAIL

- [ ] **Step 3: Write KeywordMaps**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/internal/KeywordMaps.kt
package io.github.nicekk1.opendart.internal

/** 사업보고서 keyword → API endpoint (28 entries) */
val REPORT_KEYWORD_MAP: Map<String, String> = mapOf(
    "조건부자본증권미상환" to "cndlCaplScritsNrdmpBlce",
    "미등기임원보수" to "unrstExctvMendngSttus",
    "회사채미상환" to "cprndNrdmpBlce",
    "단기사채미상환" to "srtpdPsndbtNrdmpBlce",
    "기업어음미상환" to "entrprsBilScritsNrdmpBlce",
    "채무증권발행" to "detScritsIsuAcmslt",
    "사모자금사용" to "prvsrpCptalUseDtls",
    "공모자금사용" to "pssrpCptalUseDtls",
    "임원전체보수승인" to "drctrAdtAllMendngSttusGmtsckConfmAmount",
    "임원전체보수유형" to "drctrAdtAllMendngSttusMendngPymntamtTyCl",
    "주식총수" to "stockTotqySttus",
    "회계감사" to "accnutAdtorNmNdAdtOpinion",
    "감사용역" to "adtServcCnclsSttus",
    "회계감사용역계약" to "accnutAdtorNonAdtServcCnclsSttus",
    "사외이사" to "outcmpnyDrctrNdChangeSttus",
    "신종자본증권미상환" to "newCaplScritsNrdmpBlce",
    "증자" to "irdsSttus",
    "배당" to "alotMatter",
    "자기주식" to "tesstkAcqsDspsSttus",
    "최대주주" to "hyslrSttus",
    "최대주주변동" to "hyslrChgSttus",
    "소액주주" to "mrhlSttus",
    "임원" to "exctvSttus",
    "직원" to "empSttus",
    "임원개인보수" to "hmvAuditIndvdlBySttus",
    "임원전체보수" to "hmvAuditAllSttus",
    "개인별보수" to "indvdlByPay",
    "타법인출자" to "otrCprInvstmntSttus",
)

/** 주요사항보고 keyword → API endpoint (36 entries) */
val EVENT_KEYWORD_MAP: Map<String, String> = mapOf(
    "부도발생" to "dfOcr",
    "영업정지" to "bsnSp",
    "회생절차" to "ctrcvsBgrq",
    "해산사유" to "dsRsOcr",
    "유상증자" to "piicDecsn",
    "무상증자" to "fricDecsn",
    "유무상증자" to "pifricDecsn",
    "감자" to "crDecsn",
    "관리절차개시" to "bnkMngtPcbg",
    "소송" to "lwstLg",
    "해외상장결정" to "ovLstDecsn",
    "해외상장폐지결정" to "ovDlstDecsn",
    "해외상장" to "ovLst",
    "해외상장폐지" to "ovDlst",
    "전환사채발행" to "cvbdIsDecsn",
    "신주인수권부사채발행" to "bdwtIsDecsn",
    "교환사채발행" to "exbdIsDecsn",
    "관리절차중단" to "bnkMngtPcsp",
    "조건부자본증권발행" to "wdCocobdIsDecsn",
    "자산양수도" to "astInhtrfEtcPtbkOpt",
    "타법인증권양도" to "otcprStkInvscrTrfDecsn",
    "유형자산양도" to "tgastTrfDecsn",
    "유형자산양수" to "tgastInhDecsn",
    "타법인증권양수" to "otcprStkInvscrInhDecsn",
    "영업양도" to "bsnTrfDecsn",
    "영업양수" to "bsnInhDecsn",
    "자기주식취득신탁계약해지" to "tsstkAqTrctrCcDecsn",
    "자기주식취득신탁계약체결" to "tsstkAqTrctrCnsDecsn",
    "자기주식처분" to "tsstkDpDecsn",
    "자기주식취득" to "tsstkAqDecsn",
    "주식교환" to "stkExtrDecsn",
    "회사분할합병" to "cmpDvmgDecsn",
    "회사분할" to "cmpDvDecsn",
    "회사합병" to "cmpMgDecsn",
    "사채권양수" to "stkrtbdInhDecsn",
    "사채권양도결정" to "stkrtbdTrfDecsn",
)

/** 증권신고서 keyword → API endpoint (6 entries) */
val REGSTATE_KEYWORD_MAP: Map<String, String> = mapOf(
    "주식의포괄적교환이전" to "extrRs",
    "합병" to "mgRs",
    "증권예탁증권" to "stkdpRs",
    "채무증권" to "bdRs",
    "지분증권" to "estkRs",
    "분할" to "dvRs",
)
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.internal.KeywordMapsTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/io/github/nicekk1/opendart/internal/KeywordMaps.kt \
        src/test/kotlin/io/github/nicekk1/opendart/internal/KeywordMapsTest.kt
git commit -m "feat: add keyword-to-endpoint mappings for report, event, regstate"
```

---

### Task 4: Data Models

**Files:**
- Create: `src/main/kotlin/io/github/nicekk1/opendart/model/CorpCode.kt`
- Create: `src/main/kotlin/io/github/nicekk1/opendart/model/Disclosure.kt`
- Create: `src/main/kotlin/io/github/nicekk1/opendart/model/Company.kt`
- Create: `src/main/kotlin/io/github/nicekk1/opendart/model/FinancialStatement.kt`
- Create: `src/main/kotlin/io/github/nicekk1/opendart/model/SubDocument.kt`

- [ ] **Step 1: Create CorpCode**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/model/CorpCode.kt
package io.github.nicekk1.opendart.model

data class CorpCode(
    val corpCode: String,
    val corpName: String,
    val stockCode: String,
)
```

- [ ] **Step 2: Create Disclosure**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/model/Disclosure.kt
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
```

- [ ] **Step 3: Create Company**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/model/Company.kt
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
```

- [ ] **Step 4: Create FinancialStatement**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/model/FinancialStatement.kt
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
```

- [ ] **Step 5: Create SubDocument**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/model/SubDocument.kt
package io.github.nicekk1.opendart.model

data class SubDocument(
    val title: String,
    val url: String,
)
```

- [ ] **Step 6: Verify build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add src/main/kotlin/io/github/nicekk1/opendart/model/
git commit -m "feat: add data models for Disclosure, Company, FinancialStatement, SubDocument"
```

---

### Task 5: DartClient (HTTP Client Wrapper)

**Files:**
- Create: `src/main/kotlin/io/github/nicekk1/opendart/internal/DartClient.kt`
- Create: `src/test/kotlin/io/github/nicekk1/opendart/api/DartListTest.kt`

- [ ] **Step 1: Write DartClient test**

```kotlin
// src/test/kotlin/io/github/nicekk1/opendart/api/DartListTest.kt
package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.exception.DartException
import io.github.nicekk1.opendart.internal.DartClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DartClientTest {

    @Test
    fun `getJson returns parsed json when status 000`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"status":"000","message":"정상","list":[{"name":"test"}]}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = DartClient("test_key", mockEngine)
        val result = client.getJson("list.json", mapOf())
        assertEquals("000", result["status"]?.jsonPrimitive?.content)
        client.close()
    }

    @Test
    fun `getJson throws DartException when status not 000`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"status":"010","message":"등록되지 않은 인증키"}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = DartClient("bad_key", mockEngine)
        assertFailsWith<DartException> {
            client.getJson("list.json", mapOf())
        }
        client.close()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.api.DartClientTest"`
Expected: FAIL

- [ ] **Step 3: Write DartClient**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/internal/DartClient.kt
package io.github.nicekk1.opendart.internal

import io.github.nicekk1.opendart.exception.DartException
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.io.Closeable

class DartClient(
    private val apiKey: String,
    engine: HttpClientEngine = CIO.create(),
) : Closeable {

    companion object {
        const val API_BASE_URL = "https://opendart.fss.or.kr/api/"
        const val USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.3904.108 Safari/537.36"
    }

    private val json = Json { ignoreUnknownKeys = true }

    val httpClient = HttpClient(engine)

    suspend fun getJson(
        endpoint: String,
        params: Map<String, String>,
        checkStatus: Boolean = true,
    ): JsonObject {
        val response: HttpResponse = httpClient.get("$API_BASE_URL$endpoint") {
            parameter("crtfc_key", apiKey)
            params.forEach { (k, v) -> parameter(k, v) }
        }
        val body = response.bodyAsText()
        val jo = json.parseToJsonElement(body).jsonObject
        if (checkStatus) {
            val status = jo["status"]?.jsonPrimitive?.content
            if (status != null && status != "000") {
                val message = jo["message"]?.jsonPrimitive?.content ?: "Unknown error"
                throw DartException(status, message)
            }
        }
        return jo
    }

    suspend fun getBytes(endpoint: String, params: Map<String, String>): ByteArray {
        val response: HttpResponse = httpClient.get("$API_BASE_URL$endpoint") {
            parameter("crtfc_key", apiKey)
            params.forEach { (k, v) -> parameter(k, v) }
        }
        return response.readRawBytes()
    }

    override fun close() {
        httpClient.close()
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.api.DartClientTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/io/github/nicekk1/opendart/internal/DartClient.kt \
        src/test/kotlin/io/github/nicekk1/opendart/api/DartListTest.kt
git commit -m "feat: add DartClient HTTP wrapper with API key injection and status checking"
```

---

### Task 6: CorpCodeCache

**Files:**
- Create: `src/main/kotlin/io/github/nicekk1/opendart/internal/CorpCodeCache.kt`
- Create: `src/test/kotlin/io/github/nicekk1/opendart/internal/CorpCodeCacheTest.kt`

- [ ] **Step 1: Write CorpCodeCache test**

```kotlin
// src/test/kotlin/io/github/nicekk1/opendart/internal/CorpCodeCacheTest.kt
package io.github.nicekk1.opendart.internal

import io.github.nicekk1.opendart.model.CorpCode
import kotlin.test.Test
import kotlin.test.assertEquals

class CorpCodeCacheTest {

    @Test
    fun `parseCorpCodeXml parses XML correctly`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <result>
                <list>
                    <corp_code>00126380</corp_code>
                    <corp_name>삼성전자</corp_name>
                    <stock_code>005930</stock_code>
                    <modify_date>20230101</modify_date>
                </list>
                <list>
                    <corp_code>00164779</corp_code>
                    <corp_name>SK하이닉스</corp_name>
                    <stock_code>000660</stock_code>
                    <modify_date>20230101</modify_date>
                </list>
                <list>
                    <corp_code>99999999</corp_code>
                    <corp_name>비상장회사</corp_name>
                    <stock_code> </stock_code>
                    <modify_date>20230101</modify_date>
                </list>
            </result>
        """.trimIndent()

        val result = parseCorpCodeXml(xml)
        assertEquals(3, result.size)
        assertEquals("00126380", result[0].corpCode)
        assertEquals("삼성전자", result[0].corpName)
        assertEquals("005930", result[0].stockCode)
        assertEquals(" ", result[2].stockCode)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.internal.CorpCodeCacheTest"`
Expected: FAIL

- [ ] **Step 3: Write CorpCodeCache**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/internal/CorpCodeCache.kt
package io.github.nicekk1.opendart.internal

import io.github.nicekk1.opendart.model.CorpCode
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class CorpCodeCache(
    private val client: DartClient,
    private val cacheDir: Path = Path.of("docs_cache"),
) {
    private var corpCodes: List<CorpCode> = emptyList()

    suspend fun load(): List<CorpCode> {
        if (corpCodes.isNotEmpty()) return corpCodes

        Files.createDirectories(cacheDir)
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val cacheFile = cacheDir.resolve("opendartreader_corp_codes_$today.dat")

        // Clean old cache files
        cacheDir.toFile().listFiles()?.filter {
            it.name.startsWith("opendartreader_corp_codes_") && it.name != cacheFile.fileName.toString()
        }?.forEach { it.delete() }

        if (Files.exists(cacheFile)) {
            val xml = Files.readString(cacheFile)
            corpCodes = parseCorpCodeXml(xml)
        } else {
            val bytes = client.getBytes("corpCode.xml", emptyMap())
            val xml = extractXmlFromZip(bytes)
            Files.writeString(cacheFile, xml)
            corpCodes = parseCorpCodeXml(xml)
        }
        return corpCodes
    }

    fun findCorpCode(corp: String): String? {
        if (corp.all { it.isDigit() } && corp.length == 6) {
            // stock code
            return corpCodes.firstOrNull { it.stockCode.trim() == corp }?.corpCode
        }
        if (corp.all { it.isDigit() }) {
            // corp code
            return corpCodes.firstOrNull { it.corpCode == corp }?.corpCode
        }
        // corp name
        return corpCodes.firstOrNull { it.corpName == corp }?.corpCode
    }

    fun findByName(name: String): List<CorpCode> {
        return corpCodes.filter { it.corpName.contains(name) }
    }

    private fun extractXmlFromZip(bytes: ByteArray): String {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            val entry = zis.nextEntry
            if (entry != null) {
                return zis.readBytes().toString(Charsets.UTF_8)
            }
        }
        throw IllegalStateException("Empty zip file")
    }
}

fun parseCorpCodeXml(xml: String): List<CorpCode> {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val doc = builder.parse(xml.byteInputStream())
    val nodeList = doc.getElementsByTagName("list")

    val result = mutableListOf<CorpCode>()
    for (i in 0 until nodeList.length) {
        val node = nodeList.item(i)
        val children = node.childNodes
        var corpCode = ""
        var corpName = ""
        var stockCode = ""
        for (j in 0 until children.length) {
            val child = children.item(j)
            when (child.nodeName) {
                "corp_code" -> corpCode = child.textContent ?: ""
                "corp_name" -> corpName = child.textContent ?: ""
                "stock_code" -> stockCode = child.textContent ?: ""
            }
        }
        result.add(CorpCode(corpCode, corpName, stockCode))
    }
    return result
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.internal.CorpCodeCacheTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/io/github/nicekk1/opendart/internal/CorpCodeCache.kt \
        src/test/kotlin/io/github/nicekk1/opendart/internal/CorpCodeCacheTest.kt
git commit -m "feat: add CorpCodeCache with XML parsing and daily file cache"
```

---

### Task 7: DartList API (list, company, document)

**Files:**
- Create: `src/main/kotlin/io/github/nicekk1/opendart/api/DartList.kt`

- [ ] **Step 1: Write DartList test**

Add to the existing test file or create a new one:

```kotlin
// src/test/kotlin/io/github/nicekk1/opendart/api/DartListTest.kt
// (append to existing file, or replace DartClientTest file)
package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.exception.DartException
import io.github.nicekk1.opendart.internal.DartClient
import io.github.nicekk1.opendart.model.Disclosure
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DartClientTest {

    @Test
    fun `getJson returns parsed json when status 000`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"status":"000","message":"정상","list":[{"name":"test"}]}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = DartClient("test_key", mockEngine)
        val result = client.getJson("list.json", mapOf())
        assertEquals("000", result["status"]?.jsonPrimitive?.content)
        client.close()
    }

    @Test
    fun `getJson throws DartException when status not 000`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"status":"010","message":"등록되지 않은 인증키"}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = DartClient("bad_key", mockEngine)
        assertFailsWith<DartException> {
            client.getJson("list.json", mapOf())
        }
        client.close()
    }
}

class DartListTest {

    private fun mockClientWithResponse(jsonResponse: String): DartClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(jsonResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return DartClient("test_key", mockEngine)
    }

    @Test
    fun `list returns disclosures from single page response`() = runTest {
        val json = """{
            "status":"000","message":"정상","page_no":1,"page_count":100,"total_count":1,"total_page":1,
            "list":[{"corp_cls":"Y","corp_name":"삼성전자","corp_code":"00126380","stock_code":"005930",
                     "rcept_no":"20240101000001","report_nm":"사업보고서","rcept_dt":"20240101","flr_nm":"삼성전자","rm":""}]
        }"""
        val client = mockClientWithResponse(json)
        val result = DartList.list(client, corpCode = "00126380")
        assertEquals(1, result.size)
        assertEquals("삼성전자", result[0].corpName)
        assertEquals("20240101000001", result[0].rceptNo)
        client.close()
    }

    @Test
    fun `list returns empty list when no results`() = runTest {
        val json = """{"status":"013","message":"조회된 데이터가 없습니다."}"""
        val client = mockClientWithResponse(json)
        val result = DartList.list(client, corpCode = "00126380")
        assertEquals(0, result.size)
        client.close()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.api.DartListTest"`
Expected: FAIL

- [ ] **Step 3: Write DartList**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/api/DartList.kt
package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.internal.DartClient
import io.github.nicekk1.opendart.internal.defaultEnd
import io.github.nicekk1.opendart.internal.defaultStart
import io.github.nicekk1.opendart.internal.formatDate
import io.github.nicekk1.opendart.model.Company
import io.github.nicekk1.opendart.model.Disclosure
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream

object DartList {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun list(
        client: DartClient,
        corpCode: String = "",
        start: LocalDate? = null,
        end: LocalDate? = null,
        kind: String = "",
        kindDetail: String = "",
        final: Boolean = true,
    ): List<Disclosure> {
        val params = mutableMapOf(
            "corp_code" to corpCode,
            "bgn_de" to formatDate(start ?: defaultStart()),
            "end_de" to formatDate(end ?: defaultEnd()),
            "last_reprt_at" to if (final) "Y" else "N",
            "page_no" to "1",
            "page_count" to "100",
        )
        if (kind.isNotEmpty()) params["pblntf_ty"] = kind
        if (kindDetail.isNotEmpty()) params["pblntf_detail_ty"] = kindDetail

        val jo = try {
            client.getJson("list.json", params)
        } catch (e: io.github.nicekk1.opendart.exception.DartException) {
            if (e.status == "013") return emptyList() // no data
            throw e
        }

        val listArray = jo["list"]?.jsonArray ?: return emptyList()
        val result = listArray.map { json.decodeFromJsonElement<Disclosure>(it) }.toMutableList()

        val totalPage = jo["total_page"]?.jsonPrimitive?.int ?: 1
        for (page in 2..totalPage) {
            delay(100) // rate limiting (0.1s like original)
            params["page_no"] = page.toString()
            val pageJo = client.getJson("list.json", params)
            val pageList = pageJo["list"]?.jsonArray ?: break
            result.addAll(pageList.map { json.decodeFromJsonElement<Disclosure>(it) })
        }
        return result
    }

    suspend fun company(client: DartClient, corpCode: String): Company {
        val jo = client.getJson("company.json", mapOf("corp_code" to corpCode))
        return json.decodeFromJsonElement(jo)
    }

    suspend fun companyByName(client: DartClient, corpCodeList: List<String>): List<Company> {
        return corpCodeList.map { corpCode ->
            company(client, corpCode)
        }
    }

    suspend fun document(client: DartClient, rcpNo: String): String {
        val bytes = client.getBytes("document.xml", mapOf("rcept_no" to rcpNo))
        val xmlBytes = extractFirstFromZip(bytes)
        return decodeXml(xmlBytes)
    }

    suspend fun documentAll(client: DartClient, rcpNo: String): List<String> {
        val bytes = client.getBytes("document.xml", mapOf("rcept_no" to rcpNo))
        return extractAllFromZip(bytes).map { decodeXml(it) }
    }

    private fun extractFirstFromZip(bytes: ByteArray): ByteArray {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            val entries = mutableListOf<Pair<String, ByteArray>>()
            var entry = zis.nextEntry
            while (entry != null) {
                entries.add(entry.name to zis.readBytes())
                entry = zis.nextEntry
            }
            entries.sortBy { it.first }
            return entries.firstOrNull()?.second
                ?: throw IllegalStateException("Empty zip file")
        }
    }

    private fun extractAllFromZip(bytes: ByteArray): List<ByteArray> {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            val entries = mutableListOf<Pair<String, ByteArray>>()
            var entry = zis.nextEntry
            while (entry != null) {
                entries.add(entry.name to zis.readBytes())
                entry = zis.nextEntry
            }
            entries.sortBy { it.first }
            return entries.map { it.second }
        }
    }

    private fun decodeXml(data: ByteArray): String {
        return try {
            data.toString(Charsets.forName("EUC-KR"))
        } catch (e: Exception) {
            try {
                data.toString(Charsets.UTF_8)
            } catch (e2: Exception) {
                String(data)
            }
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.api.DartListTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/io/github/nicekk1/opendart/api/DartList.kt \
        src/test/kotlin/io/github/nicekk1/opendart/api/DartListTest.kt
git commit -m "feat: add DartList API for disclosure list, company, and document"
```

---

### Task 8: DartReport, DartShare, DartEvent, DartRegState APIs

**Files:**
- Create: `src/main/kotlin/io/github/nicekk1/opendart/api/DartReport.kt`
- Create: `src/main/kotlin/io/github/nicekk1/opendart/api/DartShare.kt`
- Create: `src/main/kotlin/io/github/nicekk1/opendart/api/DartEvent.kt`
- Create: `src/main/kotlin/io/github/nicekk1/opendart/api/DartRegState.kt`
- Create: `src/test/kotlin/io/github/nicekk1/opendart/api/DartReportTest.kt`

- [ ] **Step 1: Write DartReport test**

```kotlin
// src/test/kotlin/io/github/nicekk1/opendart/api/DartReportTest.kt
package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.internal.DartClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DartReportTest {

    @Test
    fun `report returns list of maps`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"status":"000","message":"정상","list":[{"se":"배당","thstrm":"1000"}]}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = DartClient("test_key", mockEngine)
        val result = DartReport.report(client, "00126380", "배당", 2023)
        assertEquals(1, result.size)
        assertEquals("배당", result[0]["se"])
        assertEquals("1000", result[0]["thstrm"])
        client.close()
    }

    @Test
    fun `report throws on invalid keyword`() = runTest {
        val mockEngine = MockEngine { _ -> respond("") }
        val client = DartClient("test_key", mockEngine)
        assertFailsWith<IllegalArgumentException> {
            DartReport.report(client, "00126380", "없는키워드", 2023)
        }
        client.close()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.api.DartReportTest"`
Expected: FAIL

- [ ] **Step 3: Write DartReport**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/api/DartReport.kt
package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.internal.DartClient
import io.github.nicekk1.opendart.internal.REPORT_KEYWORD_MAP
import kotlinx.serialization.json.*

object DartReport {

    suspend fun report(
        client: DartClient,
        corpCode: String,
        keyWord: String,
        bsnsYear: Int,
        reprtCode: String = "11011",
    ): List<Map<String, String>> {
        val endpoint = REPORT_KEYWORD_MAP[keyWord]
            ?: throw IllegalArgumentException("Invalid key_word: '$keyWord'. Use one of: ${REPORT_KEYWORD_MAP.keys}")

        val jo = try {
            client.getJson("$endpoint.json", mapOf(
                "corp_code" to corpCode,
                "bsns_year" to bsnsYear.toString(),
                "reprt_code" to reprtCode,
            ))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) {
            return emptyList()
        }

        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { item ->
            item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }
        }
    }
}
```

- [ ] **Step 4: Write DartShare**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/api/DartShare.kt
package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.internal.DartClient
import kotlinx.serialization.json.*

object DartShare {

    suspend fun majorShareholders(client: DartClient, corpCode: String): List<Map<String, String>> {
        val jo = try {
            client.getJson("majorstock.json", mapOf("corp_code" to corpCode))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) {
            return emptyList()
        }
        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { item ->
            item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }
        }
    }

    suspend fun majorShareholdersExec(client: DartClient, corpCode: String): List<Map<String, String>> {
        val jo = try {
            client.getJson("elestock.json", mapOf("corp_code" to corpCode))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) {
            return emptyList()
        }
        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { item ->
            item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }
        }
    }
}
```

- [ ] **Step 5: Write DartEvent**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/api/DartEvent.kt
package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.internal.*
import kotlinx.serialization.json.*
import java.time.LocalDate

object DartEvent {

    suspend fun event(
        client: DartClient,
        corpCode: String,
        keyWord: String,
        start: LocalDate? = null,
        end: LocalDate? = null,
    ): List<Map<String, String>> {
        val endpoint = EVENT_KEYWORD_MAP[keyWord]
            ?: throw IllegalArgumentException("Invalid key_word: '$keyWord'. Use one of: ${EVENT_KEYWORD_MAP.keys}")

        val jo = try {
            client.getJson("$endpoint.json", mapOf(
                "corp_code" to corpCode,
                "bgn_de" to formatDate(start ?: defaultStart()),
                "end_de" to formatDate(end ?: defaultEnd()),
            ))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) {
            return emptyList()
        }

        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { item ->
            item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }
        }
    }
}
```

- [ ] **Step 6: Write DartRegState**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/api/DartRegState.kt
package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.internal.*
import kotlinx.serialization.json.*
import java.time.LocalDate

object DartRegState {

    suspend fun regstate(
        client: DartClient,
        corpCode: String,
        keyWord: String,
        start: LocalDate? = null,
        end: LocalDate? = null,
    ): List<Map<String, String>> {
        val endpoint = REGSTATE_KEYWORD_MAP[keyWord]
            ?: throw IllegalArgumentException("Invalid key_word: '$keyWord'. Use one of: ${REGSTATE_KEYWORD_MAP.keys}")

        val jo = try {
            client.getJson("$endpoint.json", mapOf(
                "corp_code" to corpCode,
                "bgn_de" to formatDate(start ?: defaultStart()),
                "end_de" to formatDate(end ?: defaultEnd()),
            ))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) {
            return emptyList()
        }

        // regstate can return either 'list' or 'group' structure
        val list = jo["list"]?.jsonArray
        if (list != null) {
            return list.map { item ->
                item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }
            }
        }

        val group = jo["group"]?.jsonArray
        if (group != null) {
            val result = mutableListOf<Map<String, String>>()
            for (g in group) {
                val title = g.jsonObject["title"]?.jsonPrimitive?.contentOrNull ?: ""
                val innerList = g.jsonObject["list"]?.jsonArray ?: continue
                for (item in innerList) {
                    val map = item.jsonObject.entries.associate { (k, v) ->
                        k to (v.jsonPrimitive.contentOrNull ?: "")
                    }.toMutableMap()
                    map["title"] = title
                    result.add(map)
                }
            }
            return result
        }

        return emptyList()
    }
}
```

- [ ] **Step 7: Run tests**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.api.DartReportTest"`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add src/main/kotlin/io/github/nicekk1/opendart/api/DartReport.kt \
        src/main/kotlin/io/github/nicekk1/opendart/api/DartShare.kt \
        src/main/kotlin/io/github/nicekk1/opendart/api/DartEvent.kt \
        src/main/kotlin/io/github/nicekk1/opendart/api/DartRegState.kt \
        src/test/kotlin/io/github/nicekk1/opendart/api/DartReportTest.kt
git commit -m "feat: add DartReport, DartShare, DartEvent, DartRegState APIs"
```

---

### Task 9: DartFinState API

**Files:**
- Create: `src/main/kotlin/io/github/nicekk1/opendart/api/DartFinState.kt`
- Create: `src/test/kotlin/io/github/nicekk1/opendart/api/DartFinStateTest.kt`

- [ ] **Step 1: Write DartFinState test**

```kotlin
// src/test/kotlin/io/github/nicekk1/opendart/api/DartFinStateTest.kt
package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.internal.DartClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DartFinStateTest {

    @Test
    fun `finstate single company returns financial statements`() = runTest {
        val jsonResponse = """{
            "status":"000","message":"정상",
            "list":[{"rcept_no":"20240101000001","corp_code":"00126380","corp_name":"삼성전자",
                     "stock_code":"005930","reprt_code":"11011","bsns_year":"2023",
                     "fs_div":"CFS","fs_nm":"연결재무제표","sj_div":"BS","sj_nm":"재무상태표",
                     "account_nm":"자산총계","thstrm_nm":"제55기","thstrm_amount":"100000",
                     "frmtrm_nm":"제54기","frmtrm_amount":"90000",
                     "bfefrmtrm_nm":"제53기","bfefrmtrm_amount":"80000","ord":"1","currency":"KRW"}]
        }"""
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(jsonResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = DartClient("test_key", mockEngine)
        val result = DartFinState.finstate(client, "00126380", 2023)
        assertEquals(1, result.size)
        assertEquals("삼성전자", result[0].corpName)
        assertEquals("자산총계", result[0].accountNm)
        client.close()
    }

    @Test
    fun `finstate returns empty list when no data`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"status":"013","message":"조회된 데이터가 없습니다."}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = DartClient("test_key", mockEngine)
        val result = DartFinState.finstate(client, "00126380", 2010)
        assertEquals(0, result.size)
        client.close()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.api.DartFinStateTest"`
Expected: FAIL

- [ ] **Step 3: Write DartFinState**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/api/DartFinState.kt
package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.internal.DartClient
import io.github.nicekk1.opendart.model.FinancialStatement
import kotlinx.serialization.json.*
import java.nio.file.Files
import java.nio.file.Path

object DartFinState {

    private val json = Json { ignoreUnknownKeys = true }

    private val REPRT_CODE_MAP = mapOf(
        "11013" to "1분기보고서",
        "11012" to "반기보고서",
        "11014" to "3분기보고서",
        "11011" to "사업보고서",
    )

    private val FS_DIV_MAP = mapOf(
        "CFS" to "연결재무제표",
        "OFS" to "별도(개별)재무제표",
    )

    suspend fun finstate(
        client: DartClient,
        corpCode: String,
        bsnsYear: Int,
        reprtCode: String = "11011",
    ): List<FinancialStatement> {
        val isMulti = "," in corpCode
        val endpoint = if (isMulti) "fnlttMultiAcnt.json" else "fnlttSinglAcnt.json"

        val jo = try {
            client.getJson(endpoint, mapOf(
                "corp_code" to corpCode,
                "bsns_year" to bsnsYear.toString(),
                "reprt_code" to reprtCode,
            ))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) {
            return emptyList()
        }

        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { json.decodeFromJsonElement<FinancialStatement>(it) }
    }

    suspend fun finstateAll(
        client: DartClient,
        corpCode: String,
        bsnsYear: Int,
        reprtCode: String = "11011",
        fsDiv: String = "CFS",
    ): List<FinancialStatement> {
        require(reprtCode in REPRT_CODE_MAP) {
            "Invalid reprt_code. Use one of: $REPRT_CODE_MAP"
        }
        require(fsDiv in FS_DIV_MAP) {
            "Invalid fs_div. Use one of: $FS_DIV_MAP"
        }

        val jo = try {
            client.getJson("fnlttSinglAcntAll.json", mapOf(
                "corp_code" to corpCode,
                "bsns_year" to bsnsYear.toString(),
                "reprt_code" to reprtCode,
                "fs_div" to fsDiv,
            ))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) {
            return emptyList()
        }

        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { json.decodeFromJsonElement<FinancialStatement>(it) }
    }

    suspend fun finstateXml(
        client: DartClient,
        rcpNo: String,
        savePath: Path,
    ): Boolean {
        val bytes = client.getBytes("fnlttXbrl.xml", mapOf("rcept_no" to rcpNo))
        Files.write(savePath, bytes)
        return true
    }

    suspend fun xbrlTaxonomy(
        client: DartClient,
        sjDiv: String,
    ): List<Map<String, String>> {
        val jo = try {
            client.getJson("xbrlTaxonomy.json", mapOf("sj_div" to sjDiv))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) {
            return emptyList()
        }
        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { item ->
            item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.api.DartFinStateTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/io/github/nicekk1/opendart/api/DartFinState.kt \
        src/test/kotlin/io/github/nicekk1/opendart/api/DartFinStateTest.kt
git commit -m "feat: add DartFinState API for financial statements and XBRL"
```

---

### Task 10: DartScraper (Web Scraping)

**Files:**
- Create: `src/main/kotlin/io/github/nicekk1/opendart/scraping/DartScraper.kt`
- Create: `src/test/kotlin/io/github/nicekk1/opendart/scraping/DartScraperTest.kt`

- [ ] **Step 1: Write DartScraper test**

```kotlin
// src/test/kotlin/io/github/nicekk1/opendart/scraping/DartScraperTest.kt
package io.github.nicekk1.opendart.scraping

import kotlin.test.Test
import kotlin.test.assertEquals

class DartScraperTest {

    @Test
    fun `parseSubDocsMultiPage extracts documents from JavaScript`() {
        val html = """
            <html><head><title>Test</title></head><body><script>
            node1['text'] = "사업보고서";
            node1['id'] = "001";
            node1['rcpNo'] = "20240101000001";
            node1['dcmNo'] = "100";
            node1['eleId'] = "200";
            node1['offset'] = "300";
            node1['length'] = "400";
            node1['dtd'] = "dart3.xsd";
            node1['tocNo'] = "1";
            node2['text'] = "감사보고서";
            node2['id'] = "002";
            node2['rcpNo'] = "20240101000001";
            node2['dcmNo'] = "101";
            node2['eleId'] = "201";
            node2['offset'] = "301";
            node2['length'] = "401";
            node2['dtd'] = "dart3.xsd";
            node2['tocNo'] = "2";
            </script></body></html>
        """.trimIndent()

        val result = DartScraper.parseSubDocsFromHtml(html)
        assertEquals(2, result.size)
        assertEquals("사업보고서", result[0].title)
        assert(result[0].url.contains("rcpNo=20240101000001"))
        assert(result[0].url.contains("dcmNo=100"))
    }

    @Test
    fun `parseAttachDocs extracts options from dropdown`() {
        val html = """
            <html><body>
            <select id="att">
                <option value="null">선택</option>
                <option value="rcpNo=20240101000001&dcmNo=100">사업보고서 본문</option>
                <option value="rcpNo=20240101000001&dcmNo=101">감사보고서</option>
            </select>
            </body></html>
        """.trimIndent()

        val result = DartScraper.parseAttachDocsFromHtml(html)
        assertEquals(2, result.size)
        assertEquals("사업보고서 본문", result[0].title)
        assert(result[0].url.contains("rcpNo=20240101000001"))
    }

    @Test
    fun `parseAttachFiles extracts file links from table`() {
        val html = """
            <html><body>
            <table><tbody>
            <tr><td>report.xls</td><td><a href="/pdf/download/excel.do?rcp_no=123">다운로드</a></td></tr>
            <tr><td>data.pdf</td><td><a href="/pdf/download/pdf.do?rcp_no=123">다운로드</a></td></tr>
            </tbody></table>
            </body></html>
        """.trimIndent()

        val result = DartScraper.parseAttachFilesFromHtml(html)
        assertEquals(2, result.size)
        assertEquals("http://dart.fss.or.kr/pdf/download/excel.do?rcp_no=123", result["report.xls"])
    }

    @Test
    fun `sortByMatch sorts by similarity`() {
        val docs = listOf(
            io.github.nicekk1.opendart.model.SubDocument("감사보고서", "url1"),
            io.github.nicekk1.opendart.model.SubDocument("사업보고서", "url2"),
            io.github.nicekk1.opendart.model.SubDocument("반기보고서", "url3"),
        )
        val sorted = DartScraper.sortByMatch(docs, "사업보고서")
        assertEquals("사업보고서", sorted[0].title)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.scraping.DartScraperTest"`
Expected: FAIL

- [ ] **Step 3: Write DartScraper**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/scraping/DartScraper.kt
package io.github.nicekk1.opendart.scraping

import io.github.nicekk1.opendart.internal.DartClient
import io.github.nicekk1.opendart.model.Disclosure
import io.github.nicekk1.opendart.model.SubDocument
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DartScraper {

    private const val DART_BASE = "http://dart.fss.or.kr"

    private val MULTI_PAGE_RE = Regex(
        """\s+node[12]\['text'\]\s*=\s*"(.*?)";\s+node[12]\['id'\]\s*=\s*"(\d+)";\s+node[12]\['rcpNo'\]\s*=\s*"(\d+)";\s+node[12]\['dcmNo'\]\s*=\s*"(\d+)";\s+node[12]\['eleId'\]\s*=\s*"(\d+)";\s+node[12]\['offset'\]\s*=\s*"(\d+)";\s+node[12]\['length'\]\s*=\s*"(\d+)";\s+node[12]\['dtd'\]\s*=\s*"(.*?)";\s+node[12]\['tocNo'\]\s*=\s*"(\d+)";"""
    )

    private val SINGLE_PAGE_RE = Regex(
        """\t\tviewDoc\('(\d+)', '(\d+)', '(\d+)', '(\d+)', '(\d+)', '(\S+)',''\);"""
    )

    suspend fun listDateEx(
        client: DartClient,
        date: LocalDate? = null,
        cache: Boolean = true,
    ): List<Disclosure> {
        val d = date ?: LocalDate.now()
        val dateStr = d.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

        val cacheDir = Path.of("docs_cache")
        Files.createDirectories(cacheDir)

        val result = mutableListOf<Disclosure>()

        for (page in 1..99) {
            delay(100)
            val url = "$DART_BASE/dsac001/search.ax?selectDate=$dateStr&pageGrouping=A&currentPage=$page"

            val html = if (cache) {
                val cacheFile = cacheDir.resolve(URLEncoder.encode(url, "UTF-8"))
                if (Files.exists(cacheFile) && Files.size(cacheFile) > 0) {
                    Files.readString(cacheFile)
                } else {
                    val text = fetchHtml(client, url)
                    Files.writeString(cacheFile, text)
                    text
                }
            } else {
                fetchHtml(client, url)
            }

            if ("검색된 자료가 없습니다" in html) break

            val doc = Jsoup.parse(html)
            val trs = doc.select("table tbody tr")
            for (tr in trs) {
                val tds = tr.select("td")
                if (tds.size < 6) continue
                val hhmm = tds[0].text().trim()
                val corpClass = tds[1].select("span span").text()
                val name = tds[1].select("span a").text().trim()
                val rcpNo = tds[2].select("a").attr("href").split("=").last()
                val title = tds[2].select("a").text().replace("\\s+".toRegex(), " ").trim()
                val frName = tds[3].text()
                val rcpDate = tds[4].text().replace(".", "-")
                val remark = tds[5].select("span").joinToString("") { it.text() }
                val dt = d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " " + hhmm

                result.add(Disclosure(
                    corpCls = corpClass,
                    corpName = name,
                    rceptNo = rcpNo,
                    reportNm = title,
                    rceptDt = dt,
                    flrNm = frName,
                    rm = remark,
                ))
            }
        }
        return result
    }

    suspend fun subDocs(
        client: DartClient,
        rcpNo: String,
        match: String? = null,
    ): List<SubDocument> {
        val url = if (rcpNo.startsWith("http")) rcpNo
                  else "$DART_BASE/dsaf001/main.do?rcpNo=$rcpNo"
        val html = fetchHtml(client, url)
        val docs = parseSubDocsFromHtml(html)
        return if (match != null) sortByMatch(docs, match) else docs
    }

    suspend fun attachDocs(
        client: DartClient,
        rcpNo: String,
        match: String? = null,
    ): List<SubDocument> {
        val url = "$DART_BASE/dsaf001/main.do?rcpNo=$rcpNo"
        val html = fetchHtml(client, url)
        val docs = parseAttachDocsFromHtml(html)
        return if (match != null) sortByMatch(docs, match) else docs
    }

    suspend fun attachFiles(
        client: DartClient,
        arg: String,
    ): Map<String, String> {
        val url = if (arg.startsWith("http")) arg
                  else "$DART_BASE/dsaf001/main.do?rcpNo=$arg"
        val html = fetchHtml(client, url)

        val rcpDcmRe = Regex(
            """\s+node[12]\['rcpNo'\]\s*=\s*"(\d+)";\s+node[12]\['dcmNo'\]\s*=\s*"(\d+)";"""
        )
        val match = rcpDcmRe.find(html) ?: return emptyMap()
        val rcpNo = match.groupValues[1]
        val dcmNo = match.groupValues[2]

        val downloadUrl = "$DART_BASE/pdf/download/main.do?rcp_no=$rcpNo&dcm_no=$dcmNo"
        val downloadHtml = fetchHtml(client, downloadUrl)
        return parseAttachFilesFromHtml(downloadHtml)
    }

    suspend fun download(
        client: DartClient,
        url: String,
        filename: String? = null,
    ): Path {
        val fn = filename ?: url.split("/").last()
        val response = client.httpClient.get(url) {
            header("User-Agent", DartClient.USER_AGENT)
        }
        val bytes = response.readRawBytes()
        val path = Path.of(fn)
        Files.write(path, bytes)
        return path
    }

    // --- Parsing functions (internal, exposed for testing) ---

    fun parseSubDocsFromHtml(html: String): List<SubDocument> {
        val matches = MULTI_PAGE_RE.findAll(html).toList()
        if (matches.isNotEmpty()) {
            return matches.map { m ->
                val (title, _, rcpNo, dcmNo, eleId, offset, length, dtd) = m.destructured
                val params = "rcpNo=$rcpNo&dcmNo=$dcmNo&eleId=$eleId&offset=$offset&length=$length&dtd=$dtd"
                SubDocument(title, "$DART_BASE/report/viewer.do?$params")
            }
        }

        val singleMatches = SINGLE_PAGE_RE.findAll(html).toList()
        if (singleMatches.isNotEmpty()) {
            val doc = Jsoup.parse(html)
            val docTitle = doc.title().trim()
            val m = singleMatches[0]
            val (rcpNo, dcmNo, eleId, offset, length, dtd) = m.destructured
            val params = "rcpNo=$rcpNo&dcmNo=$dcmNo&eleId=$eleId&offset=$offset&length=$length&dtd=$dtd"
            return listOf(SubDocument(docTitle, "$DART_BASE/report/viewer.do?$params"))
        }

        return emptyList()
    }

    fun parseAttachDocsFromHtml(html: String): List<SubDocument> {
        val doc = Jsoup.parse(html)
        val att = doc.selectFirst("#att") ?: return emptyList()
        return att.select("option")
            .filter { it.attr("value") != "null" }
            .map { opt ->
                val title = opt.text().replace("\\s+".toRegex(), " ").trim()
                val url = "$DART_BASE/dsaf001/main.do?${opt.attr("value")}"
                SubDocument(title, url)
            }
    }

    fun parseAttachFilesFromHtml(html: String): Map<String, String> {
        val doc = Jsoup.parse(html)
        val table = doc.selectFirst("table") ?: return emptyMap()
        val result = mutableMapOf<String, String>()
        for (tr in table.select("tbody tr")) {
            val tds = tr.select("td")
            if (tds.size < 2) continue
            val fname = tds[0].text()
            val href = tds[1].select("a").attr("href")
            if (href.isNotEmpty()) {
                result[fname] = "$DART_BASE$href"
            }
        }
        return result
    }

    fun sortByMatch(docs: List<SubDocument>, match: String): List<SubDocument> {
        return docs.sortedByDescending { similarity(it.title, match) }
    }

    private fun similarity(a: String, b: String): Double {
        if (a.isEmpty() && b.isEmpty()) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val maxLen = maxOf(a.length, b.length)
        val common = a.zip(b).count { (c1, c2) -> c1 == c2 }
        // Simple ratio similar to Python's difflib.SequenceMatcher
        val matches = lcs(a, b)
        return 2.0 * matches / (a.length + b.length)
    }

    private fun lcs(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1] + 1
                           else maxOf(dp[i - 1][j], dp[i][j - 1])
            }
        }
        return dp[a.length][b.length]
    }

    private suspend fun fetchHtml(client: DartClient, url: String): String {
        val response = client.httpClient.get(url) {
            header("User-Agent", DartClient.USER_AGENT)
        }
        return response.bodyAsText()
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.scraping.DartScraperTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/io/github/nicekk1/opendart/scraping/DartScraper.kt \
        src/test/kotlin/io/github/nicekk1/opendart/scraping/DartScraperTest.kt
git commit -m "feat: add DartScraper for web scraping (listDateEx, subDocs, attachDocs, attachFiles)"
```

---

### Task 11: OpenDartReader Facade

**Files:**
- Create: `src/main/kotlin/io/github/nicekk1/opendart/OpenDartReader.kt`
- Create: `src/test/kotlin/io/github/nicekk1/opendart/OpenDartReaderTest.kt`

- [ ] **Step 1: Write facade test**

```kotlin
// src/test/kotlin/io/github/nicekk1/opendart/OpenDartReaderTest.kt
package io.github.nicekk1.opendart

import io.github.nicekk1.opendart.internal.DartClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class OpenDartReaderTest {

    private fun createMockReader(): OpenDartReader {
        // Create a reader with mock engine that returns corp codes zip
        // For unit testing, we test findCorpCode logic directly
        val reader = OpenDartReader.__forTesting(
            apiKey = "test_key",
            corpCodes = listOf(
                io.github.nicekk1.opendart.model.CorpCode("00126380", "삼성전자", "005930"),
                io.github.nicekk1.opendart.model.CorpCode("00164779", "SK하이닉스", "000660"),
                io.github.nicekk1.opendart.model.CorpCode("00999999", "비상장회사", " "),
            )
        )
        return reader
    }

    @Test
    fun `findCorpCode finds by stock code`() {
        val reader = createMockReader()
        assertEquals("00126380", reader.findCorpCode("005930"))
    }

    @Test
    fun `findCorpCode finds by corp name`() {
        val reader = createMockReader()
        assertEquals("00126380", reader.findCorpCode("삼성전자"))
    }

    @Test
    fun `findCorpCode finds by corp code`() {
        val reader = createMockReader()
        assertEquals("00126380", reader.findCorpCode("00126380"))
    }

    @Test
    fun `findCorpCode returns null for unknown`() {
        val reader = createMockReader()
        assertNull(reader.findCorpCode("없는회사"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.OpenDartReaderTest"`
Expected: FAIL

- [ ] **Step 3: Write OpenDartReader facade**

```kotlin
// src/main/kotlin/io/github/nicekk1/opendart/OpenDartReader.kt
package io.github.nicekk1.opendart

import io.github.nicekk1.opendart.api.*
import io.github.nicekk1.opendart.internal.CorpCodeCache
import io.github.nicekk1.opendart.internal.DartClient
import io.github.nicekk1.opendart.model.*
import io.github.nicekk1.opendart.scraping.DartScraper
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import java.io.Closeable
import java.nio.file.Path
import java.time.LocalDate

class OpenDartReader private constructor(
    private val client: DartClient,
    private val cache: CorpCodeCache,
    private var corpCodes: List<CorpCode>,
) : Closeable {

    companion object {
        suspend fun create(apiKey: String, engine: HttpClientEngine = CIO.create()): OpenDartReader {
            val client = DartClient(apiKey, engine)
            val cache = CorpCodeCache(client)
            val corpCodes = cache.load()
            return OpenDartReader(client, cache, corpCodes)
        }

        /** For testing only - creates instance with pre-loaded corp codes */
        fun __forTesting(apiKey: String, corpCodes: List<CorpCode>): OpenDartReader {
            val client = DartClient(apiKey)
            val cache = CorpCodeCache(client)
            return OpenDartReader(client, cache, corpCodes)
        }
    }

    // === 공시정보 ===

    suspend fun list(
        corp: String? = null,
        start: LocalDate? = null,
        end: LocalDate? = null,
        kind: String = "",
        kindDetail: String = "",
        final: Boolean = true,
    ): List<Disclosure> {
        val corpCode = if (corp != null) {
            findCorpCode(corp) ?: throw IllegalArgumentException("Could not find \"$corp\"")
        } else ""
        return DartList.list(client, corpCode, start, end, kind, kindDetail, final)
    }

    suspend fun company(corp: String): Company {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("Could not find \"$corp\"")
        return DartList.company(client, corpCode)
    }

    suspend fun companyByName(name: String): List<Company> {
        val codes = cache.findByName(name).map { it.corpCode }
        return DartList.companyByName(client, codes)
    }

    suspend fun document(rcpNo: String, cache: Boolean = true): String {
        return DartList.document(client, rcpNo)
    }

    suspend fun documentAll(rcpNo: String, cache: Boolean = true): List<String> {
        return DartList.documentAll(client, rcpNo)
    }

    fun findCorpCode(corp: String): String? {
        return cache.findCorpCode(corp)
            ?: this.corpCodes.let { codes ->
                // fallback: check directly in the list
                if (!corp.all { it.isDigit() }) {
                    codes.firstOrNull { it.corpName == corp }?.corpCode
                } else if (corp.length == 6) {
                    codes.firstOrNull { it.stockCode.trim() == corp }?.corpCode
                } else {
                    codes.firstOrNull { it.corpCode == corp }?.corpCode
                }
            }
    }

    // === 사업보고서 ===

    suspend fun report(
        corp: String,
        keyWord: String,
        bsnsYear: Int,
        reprtCode: String = "11011",
    ): List<Map<String, String>> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("Could not find \"$corp\"")
        return DartReport.report(client, corpCode, keyWord, bsnsYear, reprtCode)
    }

    // === 재무제표 ===

    suspend fun finstate(
        corp: String,
        bsnsYear: Int,
        reprtCode: String = "11011",
    ): List<FinancialStatement> {
        val corpCode = if ("," in corp) {
            corp.split(",").joinToString(",") { c ->
                findCorpCode(c.trim()) ?: throw IllegalArgumentException("Could not find \"${c.trim()}\"")
            }
        } else {
            findCorpCode(corp) ?: throw IllegalArgumentException("Could not find \"$corp\"")
        }
        return DartFinState.finstate(client, corpCode, bsnsYear, reprtCode)
    }

    suspend fun finstateAll(
        corp: String,
        bsnsYear: Int,
        reprtCode: String = "11011",
        fsDiv: String = "CFS",
    ): List<FinancialStatement> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("Could not find \"$corp\"")
        return DartFinState.finstateAll(client, corpCode, bsnsYear, reprtCode, fsDiv)
    }

    suspend fun finstateXml(rcpNo: String, savePath: Path): Boolean {
        return DartFinState.finstateXml(client, rcpNo, savePath)
    }

    suspend fun xbrlTaxonomy(sjDiv: String): List<Map<String, String>> {
        return DartFinState.xbrlTaxonomy(client, sjDiv)
    }

    // === 지분공시 ===

    suspend fun majorShareholders(corp: String): List<Map<String, String>> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("Could not find \"$corp\"")
        return DartShare.majorShareholders(client, corpCode)
    }

    suspend fun majorShareholdersExec(corp: String): List<Map<String, String>> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("Could not find \"$corp\"")
        return DartShare.majorShareholdersExec(client, corpCode)
    }

    // === 주요사항보고 ===

    suspend fun event(
        corp: String,
        keyWord: String,
        start: LocalDate? = null,
        end: LocalDate? = null,
    ): List<Map<String, String>> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("Could not find \"$corp\"")
        return DartEvent.event(client, corpCode, keyWord, start, end)
    }

    // === 증권신고서 ===

    suspend fun regstate(
        corp: String,
        keyWord: String,
        start: LocalDate? = null,
        end: LocalDate? = null,
    ): List<Map<String, String>> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("Could not find \"$corp\"")
        return DartRegState.regstate(client, corpCode, keyWord, start, end)
    }

    // === 웹 스크래핑 ===

    suspend fun listDateEx(date: LocalDate? = null, cache: Boolean = true): List<Disclosure> {
        return DartScraper.listDateEx(client, date, cache)
    }

    suspend fun subDocs(rcpNo: String, match: String? = null): List<SubDocument> {
        return DartScraper.subDocs(client, rcpNo, match)
    }

    suspend fun attachDocs(rcpNo: String, match: String? = null): List<SubDocument> {
        return DartScraper.attachDocs(client, rcpNo, match)
    }

    suspend fun attachFiles(arg: String): Map<String, String> {
        return DartScraper.attachFiles(client, arg)
    }

    suspend fun download(url: String, filename: String? = null): Path {
        return DartScraper.download(client, url, filename)
    }

    override fun close() {
        client.close()
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "io.github.nicekk1.opendart.OpenDartReaderTest"`
Expected: PASS

- [ ] **Step 5: Run all tests**

Run: `./gradlew test`
Expected: All tests PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/io/github/nicekk1/opendart/OpenDartReader.kt \
        src/test/kotlin/io/github/nicekk1/opendart/OpenDartReaderTest.kt
git commit -m "feat: add OpenDartReader facade class with all public API methods"
```

---

### Task 12: Final Verification and Cleanup

**Files:**
- Modify: `CLAUDE.md` (update project description)

- [ ] **Step 1: Run full test suite**

Run: `./gradlew test`
Expected: All tests PASS

- [ ] **Step 2: Run build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Verify JAR is created**

Run: `ls -la build/libs/`
Expected: `opendart-reader-kt-0.1.0-SNAPSHOT.jar` exists

- [ ] **Step 4: Update CLAUDE.md with final project info**

Update the CLAUDE.md to reflect the completed project structure and build instructions.

- [ ] **Step 5: Final commit**

```bash
git add CLAUDE.md
git commit -m "docs: update CLAUDE.md with project build and usage info"
```
