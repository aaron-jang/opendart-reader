# opendart-reader

[![CI](https://github.com/aaron-jang/opendart-reader/actions/workflows/ci.yml/badge.svg)](https://github.com/aaron-jang/opendart-reader/actions/workflows/ci.yml)
[![Release](https://github.com/aaron-jang/opendart-reader/actions/workflows/release.yml/badge.svg)](https://github.com/aaron-jang/opendart-reader/actions/workflows/release.yml)
[![GitHub release](https://img.shields.io/github/v/release/aaron-jang/opendart-reader)](https://github.com/aaron-jang/opendart-reader/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-blue.svg)](https://kotlinlang.org)
[![JDK](https://img.shields.io/badge/JDK-17%2B-orange.svg)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green.svg)](https://spring.io/projects/spring-boot)

금융감독원 전자공시시스템(OpenDART) API를 JVM(Kotlin/Java)에서 쉽게 사용할 수 있는 라이브러리입니다.

Python 라이브러리 [OpenDartReader](https://github.com/FinanceData/OpenDartReader)의 모든 기능을 Kotlin/JVM으로 포팅했습니다.

## 특징

- OpenDART 공식 API 전체 지원 (공시정보, 사업보고서, 재무제표, 지분공시, 주요사항보고, 증권신고서)
- 웹 스크래핑 기능 포함 (날짜별 공시목록, 하위문서, 첨부파일)
- **Kotlin**: 코루틴 기반 `suspend fun` API
- **Java**: 모든 메서드에 `*Sync` 동기 버전 제공
- **Spring Boot**: 자동 설정 스타터 제공
- 타입 안전한 data class 반환
- 기업코드 자동 캐싱 (일별 갱신)

## 요구사항

- JDK 17 이상
- OpenDART API 인증키 ([발급 받기](https://opendart.fss.or.kr))

## 모듈 구성

| 모듈 | 설명 |
|------|------|
| `opendart-reader-core` | 코어 라이브러리 (Kotlin/Java) |
| `opendart-reader-spring-boot-starter` | Spring Boot 자동 설정 스타터 |

## 설치

### 코어 라이브러리 (Kotlin/Java)

#### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.aaron-jang:opendart-reader-core:0.4.0")
}
```

#### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.aaron-jang:opendart-reader-core:0.4.0'
}
```

#### Maven

```xml
<dependency>
    <groupId>io.github.aaron-jang</groupId>
    <artifactId>opendart-reader-core</artifactId>
    <version>0.4.0</version>
</dependency>
```

### Spring Boot 스타터

#### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.aaron-jang:opendart-reader-spring-boot-starter:0.4.0")
}
```

#### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.aaron-jang:opendart-reader-spring-boot-starter:0.4.0'
}
```

#### Maven

```xml
<dependency>
    <groupId>io.github.aaron-jang</groupId>
    <artifactId>opendart-reader-spring-boot-starter</artifactId>
    <version>0.4.0</version>
</dependency>
```

## 빠른 시작

### Kotlin

```kotlin
import io.github.aaronjang.opendart.OpenDartReader

// 인스턴스 생성 (코루틴 내에서)
val dart = OpenDartReader.create("YOUR_API_KEY")

// 공시 검색
val disclosures = dart.list(corp = "삼성전자")

// 기업 개황
val company = dart.company("005930")  // 종목코드로도 검색 가능

// 재무제표
val finstate = dart.finstate("삼성전자", 2023)

// 사용 후 반드시 close
dart.close()
```

### Java

```java
import io.github.aaronjang.opendart.OpenDartReader;
import io.github.aaronjang.opendart.model.*;
import java.util.List;
import java.util.Map;

// 인스턴스 생성
OpenDartReader dart = OpenDartReader.createSync("YOUR_API_KEY");

// 공시 검색
List<Disclosure> disclosures = dart.listSync("삼성전자");

// 기업 개황
Company company = dart.companySync("005930");

// 재무제표
List<FinancialStatement> finstate = dart.finstateSync("삼성전자", 2023);

// 사용 후 반드시 close
dart.close();
```

### Spring Boot

`opendart-reader-spring-boot-starter` 의존성을 추가하면 `OpenDartReader`가 자동으로 빈 등록됩니다.

**1. 프로퍼티 설정**

```yaml
# application.yml
opendart:
  api-key: YOUR_API_KEY
```

```properties
# 또는 application.properties
opendart.api-key=YOUR_API_KEY
```

**2. 주입받아 사용**

```kotlin
// Kotlin
@Service
class DartService(private val dart: OpenDartReader) {

    fun getDisclosures(corpName: String): List<Disclosure> {
        return dart.listSync(corpName)
    }

    fun getFinancials(corpName: String, year: Int): List<FinancialStatement> {
        return dart.finstateSync(corpName, year)
    }
}
```

```java
// Java
@Service
public class DartService {
    
    private final OpenDartReader dart;

    public DartService(OpenDartReader dart) {
        this.dart = dart;
    }

    public List<Disclosure> getDisclosures(String corpName) {
        return dart.listSync(corpName);
    }

    public List<FinancialStatement> getFinancials(String corpName, int year) {
        return dart.finstateSync(corpName, year);
    }
}
```

**3. 설정 옵션**

| 프로퍼티 | 필수 | 설명 |
|----------|------|------|
| `opendart.api-key` | O | OpenDART API 인증키 |

> `opendart.api-key`가 설정되지 않으면 `OpenDartReader` 빈이 등록되지 않습니다.

**4. 커스텀 빈 등록**

자동 설정 대신 직접 빈을 등록할 수도 있습니다. `@ConditionalOnMissingBean`이 적용되어 있어 사용자 빈이 우선합니다.

```kotlin
@Configuration
class MyDartConfig {

    @Bean
    fun openDartReader(): OpenDartReader {
        return OpenDartReader.createSync("YOUR_API_KEY")
    }
}
```

## 캐시

기업코드와 웹 스크래핑 결과를 캐싱합니다. `DartCache` 인터페이스를 통해 캐시 구현체를 교체할 수 있습니다.

### 기본 동작 (인메모리)

별도 설정 없이 `InMemoryDartCache`가 사용됩니다. 파일 시스템을 사용하지 않으며, 애플리케이션 재시작 시 초기화됩니다.

```kotlin
// 기본 (인메모리 캐시)
val dart = OpenDartReader.create("YOUR_API_KEY")
```

### 커스텀 캐시 주입

`DartCache` 인터페이스를 구현하여 원하는 캐시를 사용할 수 있습니다.

```kotlin
// DartCache 인터페이스
interface DartCache {
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun containsKey(key: String): Boolean
}

// 커스텀 캐시 주입
val myCache = MyCustomDartCache()
val dart = OpenDartReader.create("YOUR_API_KEY", cache = myCache)
```

### Spring Boot 캐시 연동

Spring Boot 환경에서는 `CacheManager` 빈이 있으면 자동으로 Spring Cache를 사용합니다.

| 조건 | 사용되는 캐시 |
|------|-------------|
| `CacheManager` 빈 있음 | `SpringDartCache` (Spring Cache 추상화) |
| `CacheManager` 빈 없음 | `InMemoryDartCache` (자동 폴백) |
| `DartCache` 빈 직접 등록 | 사용자 빈 우선 |

**Caffeine 사용 예시:**

```kotlin
// build.gradle.kts
implementation("org.springframework.boot:spring-boot-starter-cache")
implementation("com.github.ben-manes.caffeine:caffeine")
```

```yaml
# application.yml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=86400s
```

**Redis 사용 예시:**

```kotlin
// build.gradle.kts
implementation("org.springframework.boot:spring-boot-starter-data-redis")
```

```yaml
# application.yml
spring:
  cache:
    type: redis
  data:
    redis:
      host: localhost
      port: 6379
```

별도의 코드 변경 없이 의존성과 설정만 추가하면 캐시 구현체가 자동으로 전환됩니다.

## API 레퍼런스

### 기업 식별자

모든 `corp` 파라미터에는 다음 중 아무거나 사용할 수 있습니다:

| 형식 | 예시 | 설명 |
|------|------|------|
| 회사명 | `"삼성전자"` | 정확한 회사명 |
| 종목코드 | `"005930"` | 6자리 숫자 |
| 고유번호 | `"00126380"` | 8자리 DART 고유번호 |

### 공시정보

```kotlin
// 공시 검색 (전체)
dart.list()

// 특정 기업 공시 검색
dart.list(corp = "삼성전자", start = LocalDate.of(2024, 1, 1), end = LocalDate.of(2024, 12, 31))

// 공시 유형 필터링
dart.list(corp = "삼성전자", kind = "A")  // A=정기공시
```

**`kind` 코드:**

| 코드 | 유형 |
|------|------|
| `A` | 정기공시 |
| `B` | 주요사항보고 |
| `C` | 발행공시 |
| `D` | 지분공시 |
| `E` | 기타공시 |
| `F` | 외부감사관련 |
| `G` | 펀드공시 |
| `H` | 자산유동화 |
| `I` | 거래소공시 |
| `J` | 공정위공시 |

```kotlin
// 기업 개황
val company: Company = dart.company("삼성전자")

// 이름으로 기업 검색 (부분 매칭)
val companies: List<Company> = dart.companyByName("삼성")

// 공시서류 원본 (XML)
val xml: String = dart.document("20240101000001")

// 공시서류 원본 전체
val xmlList: List<String> = dart.documentAll("20240101000001")

// 고유번호 조회
val corpCode: String? = dart.findCorpCode("삼성전자")
```

### 사업보고서

```kotlin
// 배당 정보
dart.report("삼성전자", "배당", 2023)

// 임원 현황
dart.report("삼성전자", "임원", 2023)

// 보고서 유형 지정
dart.report("삼성전자", "배당", 2023, reprtCode = "11012")  // 반기보고서
```

**`reprtCode` 코드:**

| 코드 | 보고서 |
|------|--------|
| `11011` | 사업보고서 (기본값) |
| `11012` | 반기보고서 |
| `11013` | 1분기보고서 |
| `11014` | 3분기보고서 |

**지원 키워드 (28개):**

`조건부자본증권미상환`, `신종자본증권미상환`, `회사채미상환`, `단기사채미상환`, `기업어음미상환`, `채무증권발행`, `증자`, `감자`, `자기주식`, `최대주주`, `최대주주변동`, `소액주주`, `배당`, `미등기임원보수`, `임원`, `직원`, `임원개인보수`, `임원전체보수`, `임원전체보수승인`, `임원전체보수유형`, `개인별보수`, `사모자금사용`, `공모자금사용`, `타법인출자`, `회계감사`, `감사용역`, `회계감사용역계약`, `사외이사`, `주식총수`

### 재무제표

```kotlin
// 단일 기업 재무제표
dart.finstate("삼성전자", 2023)

// 다중 기업 재무제표 (쉼표 구분)
dart.finstate("삼성전자,SK하이닉스,현대자동차", 2023)

// 전체 재무제표 (연결)
dart.finstateAll("삼성전자", 2023, fsDiv = "CFS")

// 전체 재무제표 (별도)
dart.finstateAll("삼성전자", 2023, fsDiv = "OFS")

// XBRL 원본 파일 다운로드
dart.finstateXml("20240101000001", Path.of("finstate.zip"))

// XBRL 표준계정과목
dart.xbrlTaxonomy("BS1")
```

### 지분공시

```kotlin
// 대량보유 상황보고
dart.majorShareholders("삼성전자")

// 임원/주요주주 소유보고
dart.majorShareholdersExec("삼성전자")
```

### 주요사항보고

```kotlin
// 유상증자 결정
dart.event("삼성전자", "유상증자")

// 기간 지정
dart.event("삼성전자", "전환사채발행", 
    start = LocalDate.of(2023, 1, 1), 
    end = LocalDate.of(2023, 12, 31))
```

**지원 키워드 (36개):**

`부도발생`, `영업정지`, `회생절차`, `해산사유`, `유상증자`, `무상증자`, `유무상증자`, `감자`, `관리절차개시`, `관리절차중단`, `소송`, `해외상장결정`, `해외상장폐지결정`, `해외상장`, `해외상장폐지`, `전환사채발행`, `신주인수권부사채발행`, `교환사채발행`, `조건부자본증권발행`, `자산양수도`, `타법인증권양도`, `유형자산양도`, `유형자산양수`, `타법인증권양수`, `영업양도`, `영업양수`, `자기주식취득신탁계약해지`, `자기주식취득신탁계약체결`, `자기주식처분`, `자기주식취득`, `주식교환`, `회사분할합병`, `회사분할`, `회사합병`, `사채권양수`, `사채권양도결정`

### 증권신고서

```kotlin
dart.regstate("삼성전자", "합병")
dart.regstate("삼성전자", "채무증권", 
    start = LocalDate.of(2023, 1, 1))
```

**지원 키워드 (6개):**

`주식의포괄적교환이전`, `합병`, `증권예탁증권`, `채무증권`, `지분증권`, `분할`

### 유틸리티 (웹 스크래핑)

```kotlin
// 특정 날짜의 전체 공시 (시간 포함)
dart.listDateEx(LocalDate.of(2024, 3, 15))

// 하위 문서 목록
dart.subDocs("20240101000001")

// 유사도 순으로 정렬
dart.subDocs("20240101000001", match = "사업보고서")

// 첨부 문서 목록
dart.attachDocs("20240101000001")

// 첨부 파일 목록 (파일명 → 다운로드 URL)
val files: Map<String, String> = dart.attachFiles("20240101000001")

// 파일 다운로드
dart.download("http://dart.fss.or.kr/...", "report.xls")
```

## Java 사용법 상세

Java에서는 모든 메서드의 `*Sync` 버전을 사용합니다:

| Kotlin (suspend) | Java (동기) |
|---|---|
| `list()` | `listSync()` |
| `company()` | `companySync()` |
| `companyByName()` | `companyByNameSync()` |
| `document()` | `documentSync()` |
| `documentAll()` | `documentAllSync()` |
| `report()` | `reportSync()` |
| `finstate()` | `finstateSync()` |
| `finstateAll()` | `finstateAllSync()` |
| `finstateXml()` | `finstateXmlSync()` |
| `xbrlTaxonomy()` | `xbrlTaxonomySync()` |
| `majorShareholders()` | `majorShareholdersSync()` |
| `majorShareholdersExec()` | `majorShareholdersExecSync()` |
| `event()` | `eventSync()` |
| `regstate()` | `regstateSync()` |
| `listDateEx()` | `listDateExSync()` |
| `subDocs()` | `subDocsSync()` |
| `attachDocs()` | `attachDocsSync()` |
| `attachFiles()` | `attachFilesSync()` |
| `download()` | `downloadSync()` |

```java
// Java 예제: 삼성전자 2023년 배당 정보 조회
OpenDartReader dart = OpenDartReader.createSync("YOUR_API_KEY");

List<Map<String, String>> dividend = dart.reportSync("삼성전자", "배당", 2023);
for (Map<String, String> row : dividend) {
    System.out.println(row.get("se") + ": " + row.get("thstrm"));
}

dart.close();
```

## 데이터 모델

### Disclosure (공시 목록)

| 필드 | 타입 | 설명 |
|------|------|------|
| `corpCls` | String | 법인구분 (Y:유가, K:코스닥, N:코넥스, E:기타) |
| `corpName` | String | 회사명 |
| `corpCode` | String | 고유번호 |
| `stockCode` | String | 종목코드 |
| `rceptNo` | String | 접수번호 |
| `reportNm` | String | 보고서명 |
| `rceptDt` | String | 접수일자 |
| `flrNm` | String | 공시 제출인명 |
| `rm` | String | 비고 |

### Company (기업 개황)

| 필드 | 타입 | 설명 |
|------|------|------|
| `corpCode` | String | 고유번호 |
| `corpName` | String | 회사명 |
| `corpNameEng` | String | 영문 회사명 |
| `stockName` | String | 종목명 |
| `stockCode` | String | 종목코드 |
| `ceoNm` | String | 대표이사명 |
| `corpCls` | String | 법인구분 |
| `jurirNo` | String | 법인등록번호 |
| `bizrNo` | String | 사업자등록번호 |
| `adres` | String | 주소 |
| `hmUrl` | String | 홈페이지 |
| `irUrl` | String | IR 홈페이지 |
| `phnNo` | String | 전화번호 |
| `faxNo` | String | 팩스번호 |
| `indutyCode` | String | 업종코드 |
| `estDt` | String | 설립일 |
| `accMt` | String | 결산월 |

### FinancialStatement (재무제표)

| 필드 | 타입 | 설명 |
|------|------|------|
| `rceptNo` | String | 접수번호 |
| `corpCode` | String | 고유번호 |
| `corpName` | String | 회사명 |
| `stockCode` | String | 종목코드 |
| `reprtCode` | String | 보고서 코드 |
| `bsnsYear` | String | 사업연도 |
| `fsDiv` | String | 재무제표 구분 (CFS/OFS) |
| `fsNm` | String | 재무제표명 |
| `sjDiv` | String | 재무제표 항목 구분 |
| `sjNm` | String | 재무제표 항목명 |
| `accountNm` | String | 계정명 |
| `thstrmNm` | String | 당기명 |
| `thstrmAmount` | String | 당기금액 |
| `frmtrmNm` | String | 전기명 |
| `frmtrmAmount` | String | 전기금액 |
| `bfefrmtrmNm` | String | 전전기명 |
| `bfefrmtrmAmount` | String | 전전기금액 |
| `ord` | String | 계정과목 정렬순서 |
| `currency` | String | 통화 |

### SubDocument (하위문서/첨부파일)

| 필드 | 타입 | 설명 |
|------|------|------|
| `title` | String | 문서 제목 |
| `url` | String | 문서 URL |

## 빌드

```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# JAR 생성
./gradlew jar
```

## 기술 스택

- Kotlin (JVM 17+)
- [Ktor Client](https://ktor.io/) - HTTP 클라이언트
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) - JSON 직렬화
- [Jsoup](https://jsoup.org/) - HTML 파싱 (웹 스크래핑)

## 원본 프로젝트

이 라이브러리는 [FinanceData/OpenDartReader](https://github.com/FinanceData/OpenDartReader) (Python)의 기능을 Kotlin/JVM으로 포팅한 것입니다.

## 라이선스

MIT License
