package io.github.alstn113.assignment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.alstn113.assignment.application.dto.GetLogAnalysisResultQuery;
import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.application.dto.SubmitAnalysisResult;
import io.github.alstn113.assignment.domain.analysis.AnalysisStatus;
import io.github.alstn113.assignment.support.AbstractIntegrationTest;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

class AnalysisServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AnalysisService analysisService;

    @Test
    @DisplayName("로그 분석 요청을 보내면 비동기적으로 작업이 완료된다")
    void analyzeLogSuccessfully() throws IOException {
        // given
        String csvContent = """
                TimeGenerated [UTC],ClientIp,HttpMethod,RequestUri,UserAgent,HttpStatus,HttpVersion,ReceivedBytes,SentBytes,ClientResponseTime,SslProtocol,OriginalRequestUriWithArgs
                "1/31/2026, 7:30:15.123 AM",1.2.3.4,GET,/api/v1,Mozilla,200,HTTP/1.1,100,500,0.123,TLSv1.2,/api/v1
                """;
        MockMultipartFile file = new MockMultipartFile("file", "logs.csv", "text/csv", csvContent.getBytes());

        // when
        SubmitAnalysisResult submitResult = analysisService.analyzeLog(file);
        Long analysisId = submitResult.analysisId();

        // then
        assertThat(analysisId).isNotNull();

        // 비동기 작업 완료 대기
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    LogAnalysisResultDto result = analysisService
                            .getResult(new GetLogAnalysisResultQuery(analysisId, 10, 10, 10));
                    assertThat(result.status()).isEqualTo(AnalysisStatus.COMPLETED);
                    assertThat(result.summary().totalCount()).isEqualTo(1);
                });
    }
}
