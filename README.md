# 접속 로그 분석 및 통계 조회 서비스

## 목차

- [1. 프로젝트 개요](#1-프로젝트-개요)
- [2. 기술 스택](#2-기술-스택)
- [3. 실행 방법](#3-실행-방법)
- [4. 시스템 설계 및 구현](#4-시스템-설계-및-구현)
- [5. API 명세](#5-api-명세)
- [6. 회고](#6-회고)

## 1. 프로젝트 설명

접속 로그 파일(CSV) 업로드를 통해 로그를 분석하고, 다양한 통계 정보를 제공하는 서비스입니다.

## 2. 기술 스택

- 언어: Java 21
- 프레임워크: Spring Boot 3.5.10
- 빌드 도구: Gradle
- CSV 파싱: Apache Commons CSV
- 외부 요청: RestClient, HttpInterface
- 결함 감내: Resilience4j (Circuit Breaker, Retry, Rate Limiter)
- 캐시: Caffeine
- 테스트: JUnit 5, RestAssured
- 문서: Swagger UI

## 3. 실행 방법

Mac 을 기준으로 설명하겠습니다.

### 프로젝트 클론

```bash
git clone https://github.com/alstn113/log-analyzer.git
cd log-analyzer
```

### 환경 변수 설정 (선택 사항)

외부 API(ipinfo.io) 연동을 위한 토큰을 설정합니다. 토큰은 공식 홈페이지에서 무료로 발급받을 수 있습니다.

- 미설정 시: 애플리케이션은 정상 동작하나, 기본 제공 트래픽 초과 시 429 (Too Many Requests) 응답을 받을 수 있습니다.
- 잘못된 토큰 설정 시: 인증 실패로 인해 409 (Conflict) 응답이 발생하므로 정확한 값을 입력해 주세요.

```bash
export IPINFO_TOKEN=your_token
```

### 빌드 및 실행

```
./gradlew bootRun
```

### API 문서 확인

- 서버 실행 후 브라우저에서 아래 주소로 접속하여 API 명세 및 테스트를 진행할 수 있습니다.
- 로그 파일 업로드 및 분석 (POST 요청) -> 로그 분석 결과 조회 (GET 요청) 순서로 사용합니다.
- 로그 파일 업로드 및 분석 요청은 비동기로 처리되므로 로그 분석 결과 조회 시 PENDING 또는 PROCESSING 상태가 반환될 수 있습니다. 이 경우 잠시 후 다시 조회해 주세요.

Swagger UI: http://localhost:8080/swagger-ui/index.html

## 4. 시스템 설계 및 구현

- 패키지 구조:
  - DDD 기반 계층형 아키텍처를 채택하여 `ui`, `application`, `domain`, `infra` 계층으로 분리
  - `application` 계층에서 외부 기술인 `infra` 계층에 대한 의존하지 않도록 인터페이스를 정의하고, `infra` 계층에서 이를 구현하도록 설계 (DIP 원칙 준수)
- 핵심 로직 설계:
  - 분석 요청 처리:
    - 비동기 방식으로 분석 작업을 처리하여 빠른 응답 제공
    - 임시 파일로 저장하여 메모리 사용 최소화
  - 외부 요청:
    - RestClient, HttpInterface 사용
    - Resilience4j CircuitBreaker, Retry, RateLimiter, Timeout 설정으로 결함 감내
    - 별도 스레드풀을 사용한 병렬 처리로 응답 시간 단축
    - Caffeine 캐시 적용으로 중복 요청 방지 및 응답 시간 단축
  - CSV 파싱:
    - Apache Commons CSV 사용
    - 스트리밍 방식으로 대용량 파일 처리 시 메모리 사용 최소화
    - 파싱 오류 발생 시 오류 개수 집계 및 샘플링 처리

## 5. API 명세

Swagger UI: http://localhost:8080/swagger-ui/index.html

<details>
<summary>로그 파일 업로드 및 분석 요청 "POST: /analysis"</summary>

로그 파일을 업로드하여 분석 작업을 시작합니다. 분석은 비동기로 진행되며, 요청 즉시 분석 ID(`analysisId`)를 반환합니다. 비어있는 파일이거나 CSV 형식이 아닌 파일, 헤더가 잘못된 파일에 대해서 예외를 반환합니다. 로그 분석 후 상위 100개에 대한 IP 정보 조회를 위해 외부 API를 호출합니다.

> 참고: Swagger 문제로 파일 크기 제한에 대한 예외는 받을 수 없습니다.

- **Endpoint:** `POST /analysis`
- **Content-Type:** `multipart/form-data`

#### Parameters

| Name   | Type            | Required | Description               |
| ------ | --------------- | -------- | ------------------------- |
| `file` | `MultipartFile` | **Yes**  | 분석할 로그 파일 (`.csv`) |

#### Response Example

```json
{
  "success": true,
  "data": {
    "analysisId": 1
  },
  "error": null
}
```

</details>

<details>
<summary>로그 분석 결과 조회 "GET /analysis/{analysisId}"</summary>

발급받은 분석 ID를 통해 분석 상태와 결과를 조회합니다. 분석이 완료(`COMPLETED`)되면 상세 통계 정보를 포함하며, 진행 중(`PROCESSING`)이거나 대기 중(`PENDING`)일 때는 상태 정보만 반환됩니다. 분석 실패(`FAILED`)한 경우 결과에 에러 메세지를 포함합니다.

> totalCount는 전체 로그 수, validCount는 파싱에 성공한 로그 수를 의미합니다. 상태 코드 분포는 파싱에 성공한 유효한 로그를 기준으로 백분율(%)로 계산됩니다.

- **Endpoint:** `GET /analysis/{analysisId}`
- **Methods:** `GET`

#### Parameters

| Name             | Type      | In    | Required | Description                                          |
| ---------------- | --------- | ----- | -------- | ---------------------------------------------------- |
| `analysisId`     | `Long`    | Path  | **Yes**  | 조회할 분석 ID                                       |
| `topPaths`       | `Integer` | Query | No       | 상위 N개의 Path 조회 (기본값: 10, 범위: 1~100)       |
| `topStatusCodes` | `Integer` | Query | No       | 상위 N개의 StatusCode 조회 (기본값: 10, 범위: 1~100) |
| `topIps`         | `Integer` | Query | No       | 상위 N개의 IP 조회 (기본값: 10, 범위: 1~100)         |

#### Response Example (완료 시)

```json
{
  "success": true,
  "data": {
    "analysisId": 1,
    "status": "COMPLETED",
    "summary": {
      "totalCount": 190426,
      "validCount": 182264,
      "statusCodeDistribution": {
        "success2xx": 63.0,
        "redirect3xx": 2.7,
        "clientError4xx": 32.8,
        "serverError5xx": 1.4
      }
    },
    "parsingErrors": {
      "errorCount": 8162,
      "errorRate": 4.29
    },
    "topPaths": [
      {
        "path": "/event/banner/mir2/popup",
        "count": 26981
      },
      {
        "path": "/launcher/launcher",
        "count": 24040
      }
    ],
    "topStatusCodes": [
      {
        "statusCode": 200,
        "count": 114351
      },
      {
        "statusCode": 403,
        "count": 56161
      }
    ],
    "topIps": [
      {
        "ip": "120.242.23.238",
        "count": 20916,
        "ipInfo": {
          "country": "CN",
          "region": "Shanghai",
          "city": "Shanghai",
          "org": "AS9808 China Mobile Communications Group Co., Ltd."
        }
      },
      {
        "ip": "20.249.183.75",
        "count": 13764,
        "ipInfo": {
          "country": "KR",
          "region": "Seoul",
          "city": "Seoul",
          "org": "AS8075 Microsoft Corporation"
        }
      }
    ]
  },
  "error": null
}
```

#### Response Example (진행 중)

```json
{
  "success": true,
  "data": {
    "analysisId": 2,
    "status": "PROCESSING"
  },
  "error": null
}
```

</details>

## 6. 회고

### 가장 중요하다고 판단한 기능

- 확장성을 고려한 아키텍처 설계
  - `application` 계층에서 `LogParser` 인터페이스 등을 정의하고, `infra` 계층에서 `CsvLogParser` 구현체를 제공하는 구조로 설계함으로써, 향후 다른 형식의 로그 파일(예: JSON, XML 등)도 쉽게 추가 가능
- 예외 상황에 대한 대응
- 외부 요청
  - 병렬 처리로 순차 처리에 비해 50% 이상 응답 시간 단축
  - 결함 감내 적용으로 외부 API 장애 시에도 서비스 안정성
  - 캐시 적용으로 중복 요청 방지 및 응답 시간 단축

### 특히 신경 쓴 부분

- 예외 상황에 대한 대응
  - 비어있는 파일 또는 CSV 파일이 아닌 경우
  - 로그 파일의 헤더가 잘못된 경우
  - 파일 크기 제한(50MB)을 초과한 경우
  - 결과 조회 시 존재하지 않는 분석 ID 요청
  - 결과 조회 시 topN (topPaths, topStatusCodes, topIps) 파라미터의 범위 벗어나는 경우
- 외부 요청
  - 별도 스레드 풀을 분리하여 외부 요청을 병렬 처리하여 순차적인 요청 방식 대비 50% 이상 응답 시간 단축
  - Circuit Breaker 를 설정하여 외부 API 장애 시 불필요한 대기 시간 방지 및 Fallback 처리로 전체 분석 실패 방지
  - Rate Limiter 를 설정하여 외부 API 제공업체의 제한을 초과하지 않도록 요청 속도 제어
  - Timeout 설정으로 응답 지연 시 빠르게 실패 처리
  - Retry 설정 시 최대 2번 재시도 (지수적 백오프 적용)
  - 동일한 IP 에 대한 중복 요청을 캐싱하여 응답 시간 단축

### 실 서비스로 운영한다면 개선하거나 보완할 포인트

- 인메모리 데이터베이스에서 실제 RDBMS 로 변경
- 분석 로그 업로드 서버와 분석 서버를 분리해 확장성 및 장애 전파 방지
- 비동기 처리를 위해 임시 파일로 저장하는 것을 클라우드 스토리지(S3 등)로 변경
- 로그 분석 시 Spring Batch 를 활용해 대용량 처리 및 장애 복구 기능 추가
- 트래픽이 늘어 서버 이중화 시 IP 캐시에 대해 분산 캐시(Redis 등)와 로컬 캐시를 함께 활용
- 외부 IP 조회 시 API 호출 대신 자체 DB를 활용하는 방안 검토
