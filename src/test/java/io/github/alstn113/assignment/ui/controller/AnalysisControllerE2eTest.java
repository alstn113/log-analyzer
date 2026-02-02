package io.github.alstn113.assignment.ui.controller;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.github.alstn113.assignment.support.AbstractE2eTest;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AnalysisControllerE2eTest extends AbstractE2eTest {

    private static final String VALID_CSV_HEADER = "TimeGenerated [UTC],ClientIp,HttpMethod,RequestUri,UserAgent,HttpStatus,HttpVersion,ReceivedBytes,SentBytes,ClientResponseTime,SslProtocol,OriginalRequestUriWithArgs\n";
    private static final String VALID_CSV_ROW = "\"1/31/2026, 7:30:15.123 AM\",1.2.3.4,GET,/api/v1,Mozilla,200,HTTP/1.1,100,500,0.123,TLSv1.2,/api/v1\n";

    @Test
    @DisplayName("로그 파일을 업로드하고 분석이 완료될 때까지 기다려 결과를 조회한다 (성공 케이스)")
    void uploadAndGetResultSuccess() {
        // 1. 파일 업로드
        String csvContent = VALID_CSV_HEADER + VALID_CSV_ROW;

        Long analysisId = given()
                .multiPart("file", "test_success.csv", csvContent.getBytes(), "text/csv")
                .when()
                .post("/analysis")
                .then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .extract()
                .jsonPath()
                .getLong("data.analysisId");

        // 2. 결과 조회 (비동기이므로 COMPLETED 가 될 때까지 대기)
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> given()
                        .pathParam("analysisId", analysisId)
                        .when()
                        .get("/analysis/{analysisId}")
                        .then()
                        .statusCode(HttpStatus.OK.value())
                        .body("success", equalTo(true))
                        .body("data.status", equalTo("COMPLETED"))
                        .body("data.summary.totalCount", equalTo(1))
                        .body("data.topIps[0].ip", equalTo("1.2.3.4")));
    }

    @Test
    @DisplayName("업로드 직후에는 분석 상태가 PENDING 이거나 PROCESSING 일 수 있다")
    void uploadAndCheckImmediateStatus() {
        String csvContent = VALID_CSV_HEADER + VALID_CSV_ROW;

        Long analysisId = given()
                .multiPart("file", "test_immediate.csv", csvContent.getBytes(), "text/csv")
                .when()
                .post("/analysis")
                .then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .extract()
                .jsonPath()
                .getLong("data.analysisId");

        // 즉시 조회 시 상태 확인 (너무 빨리 끝나면 COMPLETED 일 수도 있음)
        given()
                .pathParam("analysisId", analysisId)
                .when()
                .get("/analysis/{analysisId}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", anyOf(equalTo("PENDING"), equalTo("PROCESSING"), equalTo("COMPLETED")));
    }

    @Test
    @DisplayName("빈 파일을 업로드하면 400 에러가 발생한다")
    void uploadEmptyFile() {
        given()
                .multiPart("file", "empty.csv", "".getBytes(), "text/csv")
                .when()
                .post("/analysis")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("success", equalTo(false))
                .body("error.code", equalTo("BAD_REQUEST"));
    }

    @Test
    @DisplayName("CSV 가 아닌 확장자 파일을 업로드하면 400 에러가 발생한다")
    void uploadInvalidExtension() {
        given()
                .multiPart("file", "test.txt", "some content".getBytes(), "text/plain")
                .when()
                .post("/analysis")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("success", equalTo(false))
                .body("error.code", equalTo("BAD_REQUEST"));
    }

    @Test
    @DisplayName("잘못된 헤더의 CSV 를 업로드하면 분석 결과 상태가 FAILED 가 된다")
    void uploadInvalidHeaderCsv() {
        String invalidCsv = "WrongHeader1,WrongHeader2\nValue1,Value2\n";

        Long analysisId = given()
                .multiPart("file", "invalid_header.csv", invalidCsv.getBytes(), "text/csv")
                .when()
                .post("/analysis")
                .then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .extract()
                .jsonPath()
                .getLong("data.analysisId");

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> given()
                        .pathParam("analysisId", analysisId)
                        .when()
                        .get("/analysis/{analysisId}")
                        .then()
                        .statusCode(HttpStatus.OK.value())
                        .body("data.status", equalTo("FAILED"))
                        .body("data.errorMessage", notNullValue()));
    }

    @Test
    @DisplayName("분석 결과 조회 시 파라미터 범위를 벗어나면 400 에러가 발생한다")
    void getResultWithInvalidParams() {
        // 먼저 정상적인 분석 하나 생성
        String csvContent = VALID_CSV_HEADER + VALID_CSV_ROW;
        Long analysisId = given()
                .multiPart("file", "test_params.csv", csvContent.getBytes(), "text/csv")
                .when()
                .post("/analysis")
                .then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .extract()
                .jsonPath()
                .getLong("data.analysisId");

        // 잘못된 파라미터 (topPaths=0) 로 조회
        given()
                .pathParam("analysisId", analysisId)
                .queryParam("topPaths", 0)
                .when()
                .get("/analysis/{analysisId}")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("success", equalTo(false))
                .body("error.code", equalTo("BAD_REQUEST"));

        // 잘못된 파라미터 (topIps=101) 로 조회
        given()
                .pathParam("analysisId", analysisId)
                .queryParam("topIps", 101)
                .when()
                .get("/analysis/{analysisId}")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("success", equalTo(false));
    }

    @Test
    @DisplayName("존재하지 않는 분석 ID 조회 시 404를 반환한다")
    void getResultNotFound() {
        given()
                .pathParam("analysisId", 999999L)
                .when()
                .get("/analysis/{analysisId}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("success", equalTo(false))
                .body("error.code", equalTo("ANALYSIS_NOT_FOUND"));
    }
}
