# OpenDartReaderKt - Design Spec

OpenDART(전자공시시스템) API Python 라이브러리 [OpenDartReader](https://github.com/FinanceData/OpenDartReader)를 Kotlin/JVM 라이브러리로 포팅.

## Goals

- 원본 OpenDartReader의 모든 기능을 1:1 포팅
- Kotlin/JVM 관례에 맞는 API 설계 (camelCase, data class, coroutines)
- 라이브러리로 배포 가능한 형태

## Tech Stack

| 영역 | 선택 | 이유 |
|------|------|------|
| Language | Kotlin (JVM 17+) | 프로젝트 요구사항 |
| Build | Gradle KTS | Kotlin 표준 빌드 |
| HTTP | Ktor Client (CIO engine) | Kotlin-native, 코루틴 기반 |
| JSON | kotlinx.serialization | Ktor와 자연스러운 통합 |
| HTML parsing | Jsoup | 웹 스크래핑용, JVM 표준 |
| XML | kotlin.xml (stdlib) | 기업코드 XML 파싱 |
| Test | kotlin.test + JUnit5 | Kotlin 표준 테스트 |

## API Surface

모든 네트워크 호출은 `suspend fun`. 반환 타입은 `data class` + `List<T>`.

### OpenDartReader (main facade)

```kotlin
class OpenDartReader(apiKey: String) : Closeable {

    // === 공시정보 ===
    suspend fun list(
        corp: String? = null,
        start: LocalDate? = null,
        end: LocalDate? = null,
        kind: String = "",
        kindDetail: String = "",
        final: Boolean = true
    ): List<Disclosure>

    suspend fun company(corp: String): Company
    suspend fun companyByName(name: String): List<Company>
    suspend fun document(rcpNo: String, cache: Boolean = true): String
    suspend fun documentAll(rcpNo: String, cache: Boolean = true): List<String>
    fun findCorpCode(corp: String): String?

    // === 사업보고서 (28 keywords) ===
    suspend fun report(
        corp: String,
        keyWord: String,
        bsnsYear: Int,
        reprtCode: String = "11011"
    ): List<Map<String, String>>

    // === 재무제표 ===
    suspend fun finstate(
        corp: String,       // 단일 또는 쉼표 구분 복수
        bsnsYear: Int,
        reprtCode: String = "11011"
    ): List<FinancialStatement>

    suspend fun finstateAll(
        corp: String,
        bsnsYear: Int,
        reprtCode: String = "11011",
        fsDiv: String = "CFS"
    ): List<FinancialStatement>

    suspend fun finstateXml(rcpNo: String, savePath: Path): Boolean
    suspend fun xbrlTaxonomy(sjDiv: String): List<XbrlTaxonomy>

    // === 지분공시 ===
    suspend fun majorShareholders(corp: String): List<Map<String, String>>
    suspend fun majorShareholdersExec(corp: String): List<Map<String, String>>

    // === 주요사항보고 (40 keywords) ===
    suspend fun event(
        corp: String,
        keyWord: String,
        start: LocalDate? = null,
        end: LocalDate? = null
    ): List<Map<String, String>>

    // === 증권신고서 (6 keywords) ===
    suspend fun regstate(
        corp: String,
        keyWord: String,
        start: LocalDate? = null,
        end: LocalDate? = null
    ): List<Map<String, String>>

    // === 웹 스크래핑 ===
    suspend fun listDateEx(date: LocalDate? = null, cache: Boolean = true): List<Disclosure>
    suspend fun subDocs(rcpNo: String, match: String? = null): List<SubDocument>
    suspend fun attachDocs(rcpNo: String, match: String? = null): List<SubDocument>
    suspend fun attachFiles(rcpNo: String): Map<String, String>
    suspend fun download(url: String, filename: String? = null): Path
}
```

### Design Notes

**report / event / regstate / majorShareholders 반환 타입이 `List<Map<String, String>>`인 이유:**
- 이 API들은 keyword에 따라 응답 필드가 완전히 달라짐 (report만 28종류)
- 각각 data class를 만들면 70+ 개 클래스가 필요하고 유지보수 불가
- 원본도 동적 DataFrame으로 처리하므로 Map이 적절

**finstate는 data class 사용:**
- 응답 구조가 고정되어 있고, 가장 자주 사용되는 핵심 API
- 타입 안전성의 이점이 큼

## Data Models

```kotlin
@Serializable
data class Disclosure(
    val corpCls: String = "",       // 법인구분 (Y:유가, K:코스닥, N:코넥스, E:기타)
    val corpName: String = "",      // 회사명
    val corpCode: String = "",      // 고유번호
    val stockCode: String = "",     // 종목코드
    val rceptNo: String = "",       // 접수번호
    val reportNm: String = "",      // 보고서명
    val rceptDt: String = "",       // 접수일자
    val flrNm: String = "",         // 공시 제출인명
    val rm: String = ""             // 비고
)

@Serializable
data class Company(
    val corpCode: String = "",
    val corpName: String = "",
    val corpNameEng: String = "",
    val stockName: String = "",
    val stockCode: String = "",
    val ceoNm: String = "",
    val corpCls: String = "",
    val jurirNo: String = "",
    val bizrNo: String = "",
    val adres: String = "",
    val hmUrl: String = "",
    val irUrl: String = "",
    val phnNo: String = "",
    val faxNo: String = "",
    val indutyCode: String = "",
    val estDt: String = "",
    val accMt: String = ""
)

@Serializable
data class FinancialStatement(
    val rceptNo: String = "",
    val corpCode: String = "",
    val corpName: String = "",
    val stockCode: String = "",
    val reprtCode: String = "",
    val bsnsYear: String = "",
    val fsDiv: String = "",         // CFS or OFS
    val fsNm: String = "",
    val sjDiv: String = "",
    val sjNm: String = "",
    val accountNm: String = "",
    val thstrmNm: String = "",
    val thstrmAmount: String = "",
    val frmtrmNm: String = "",
    val frmtrmAmount: String = "",
    val bfefrmtrmNm: String = "",
    val bfefrmtrmAmount: String = "",
    val ord: String = ""
)

@Serializable
data class XbrlTaxonomy(
    val sjDiv: String = "",
    val sjNm: String = "",
    val accountId: String = "",
    val accountNm: String = "",
    val accountDetail: String = "",
    val ifrsRef: String = ""
)

data class SubDocument(
    val title: String,
    val url: String
)

data class CorpCode(
    val corpCode: String,
    val corpName: String,
    val stockCode: String
)
```

## Internal Components

### DartClient
- Ktor HttpClient 래퍼
- 공통 파라미터 처리 (`crtfc_key` 자동 추가)
- API 응답 status 검증 (`"000"` = 성공)
- 에러 시 DartException throw
- 페이지네이션 자동 처리 (`list` API)
- Rate limiting (0.1초 딜레이, 원본과 동일)

### CorpCodeCache
- 기업코드 매핑 파일 캐시 (일별 갱신)
- `corpCode.xml` 다운로드 → ZIP 해제 → XML 파싱
- `docs_cache/` 디렉토리에 날짜별 저장
- 오래된 캐시 자동 정리

### DartScraper
- Jsoup 기반 HTML 파싱
- `list_date_ex`: 날짜별 공시 목록 (시간 포함)
- `sub_docs`: 공시 하위문서 (JavaScript 변수 regex 파싱)
- `attach_docs`: 첨부문서 (드롭다운 HTML 파싱)
- `attach_files`: 첨부파일 (테이블 HTML 파싱)
- User-Agent 헤더 설정 (원본과 동일)

## Keyword Mappings

원본의 keyword → API endpoint 매핑을 그대로 포팅:

- **report**: 28개 (조건부자본증권미상환, 배당, 임원, 자기주식, etc.)
- **event**: 40개 (부도발생, 유상증자, 전환사채발행, 회사합병, etc.)
- **regstate**: 6개 (주식의포괄적교환이전, 합병, 채무증권, etc.)

각 매핑은 `Map<String, String>` 상수로 정의.

## Error Handling

```kotlin
class DartException(
    val status: String,     // API 상태 코드
    val message: String     // 에러 메시지
) : RuntimeException("[$status] $message")
```

- API 응답의 `status` 필드가 `"000"`이 아니면 DartException throw
- 원본의 에러 처리 동작 유지

## Package Structure

```
io.github.opendartreader/
├── OpenDartReader.kt
├── model/           # data classes
├── internal/        # DartClient, CorpCodeCache, keyword maps
├── scraping/        # DartScraper (Jsoup)
└── exception/       # DartException
```

## Build Configuration

- Gradle KTS with kotlin("jvm") plugin
- kotlinx.serialization plugin
- Dependencies: ktor-client-cio, ktor-client-content-negotiation,
  ktor-serialization-kotlinx-json, jsoup, kotlinx-coroutines-core
- 라이브러리 배포용 maven-publish plugin 설정

## Testing Strategy

- Unit tests: keyword 매핑, 날짜 변환, corp code 파싱
- Integration tests: 실제 API 호출 (API key 필요, CI에서는 skip)
- Mock tests: Ktor MockEngine으로 HTTP 응답 모킹
