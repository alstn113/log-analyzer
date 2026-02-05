package io.github.alstn113.assignment.infra.parser;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alstn113.assignment.application.LogParser;
import io.github.alstn113.assignment.domain.analysis.vo.LogEntry;
import io.github.alstn113.assignment.support.AbstractIntegrationTest;
import java.io.File;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CsvLogParserTest extends AbstractIntegrationTest {

    @Autowired
    private CsvLogParser csvLogParser;

    @Test
    @DisplayName("CSV 로그 파일을 정상적으로 파싱하여 모델로 변환한다")
    void parseCsvSuccessfully() throws Exception {
        // given
        URL resource = getClass().getClassLoader().getResource("test_logs.csv");
        File file = new File(resource.toURI());

        // when
        try (LogParser.LogStream result = csvLogParser.parse(file)) {
            List<LogEntry> logEntries = result.logEntries().toList();

            // then
            assertThat(logEntries).hasSize(2);
            assertThat(result.parsingErrors().errorCount()).isZero();

            LogEntry first = logEntries.get(0);
            assertThat(first.clientIp()).isEqualTo("1.2.3.4");
            assertThat(first.httpStatus()).isEqualTo(200);
            assertThat(first.requestUri()).isEqualTo("/api/v1");

            // ClientResponseTime: 0.123 -> 123ms
            assertThat(first.clientResponseTime()).isEqualTo(123);
        }
    }
}
