# OpenDartReaderKt

[OpenDartReader](https://github.com/FinanceData/OpenDartReader) Python 라이브러리의 Kotlin/JVM 포팅.
OpenDART(전자공시시스템) API를 Kotlin 코루틴 기반으로 제공합니다.

## Tech Stack

- Language: Kotlin (JVM 17+)
- Build: Gradle KTS
- HTTP: Ktor Client (CIO)
- JSON: kotlinx.serialization
- HTML Parsing: Jsoup
- Test: kotlin.test + JUnit5

## Build & Test

```bash
./gradlew build    # 빌드
./gradlew test     # 테스트
```

## Package Structure

```
io.github.nicekk1.opendart/
├── OpenDartReader.kt      # 메인 파사드
├── model/                 # 데이터 클래스 (Disclosure, Company, FinancialStatement 등)
├── api/                   # API 모듈 (DartList, DartReport, DartFinState 등)
├── scraping/              # 웹 스크래핑 (DartScraper)
├── internal/              # 내부 유틸 (DartClient, CorpCodeCache, KeywordMaps)
└── exception/             # DartException
```

## Usage

```kotlin
val dart = OpenDartReader.create("YOUR_API_KEY")

// 공시 검색
val disclosures = dart.list(corp = "삼성전자")

// 재무제표
val finstate = dart.finstate("삼성전자", 2023)

// 사업보고서
val report = dart.report("삼성전자", "배당", 2023)

dart.close()
```

## Development Guidelines

- Kotlin 코딩 컨벤션을 따릅니다
- 커밋 메시지는 한글로 작성합니다
- 테스트는 구현과 함께 작성합니다
