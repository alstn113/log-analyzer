package io.github.alstn113.assignment.domain.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alstn113.assignment.domain.analysis.vo.LogEntry;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogAggregatorTest {

    @Test
    @DisplayName("로그 리스트를 기반으로 통계가 올바르게 집계된다")
    void aggregateSuccessfully() {
        // given
        LogEntry entry1 = createEntry("1.1.1.1", "/api/v1", 200);
        LogEntry entry2 = createEntry("1.1.1.1", "/api/v2", 200);
        LogEntry entry3 = createEntry("2.2.2.2", "/api/v1", 404);
        LogEntry entry4 = createEntry("3.3.3.3", "/api/v1", 500);
        LogEntry entry5 = createEntry("1.1.1.1", "/api/v1", 301);

        List<LogEntry> entries = List.of(entry1, entry2, entry3, entry4, entry5);

        // when
        LogStatistics stats = LogAggregator.aggregate(entries);

        // then
        assertThat(stats.totalRequests()).isEqualTo(5);

        // 상태 코드 비율 확인 (200: 2개, 301: 1개, 404: 1개, 500: 1개)
        // 2/5 = 40.0, 1/5 = 20.0
        assertThat(stats.statusCodeDistribution().success2xx()).isEqualTo(40.0);
        assertThat(stats.statusCodeDistribution().redirect3xx()).isEqualTo(20.0);
        assertThat(stats.statusCodeDistribution().clientError4xx()).isEqualTo(20.0);
        assertThat(stats.statusCodeDistribution().serverError5xx()).isEqualTo(20.0);

        // 상위 경로 확인 (/api/v1 은 3개)
        assertThat(stats.topPaths().get(0).path()).isEqualTo("/api/v1");
        assertThat(stats.topPaths().get(0).count()).isEqualTo(4);

        // 상위 IP 확인 (1.1.1.1 은 3개)
        assertThat(stats.topIps().get(0).ip()).isEqualTo("1.1.1.1");
        assertThat(stats.topIps().get(0).count()).isEqualTo(3);
    }

    private LogEntry createEntry(String ip, String path, int status) {
        return new LogEntry(
                LocalDateTime.now(),
                ip,
                "GET",
                path,
                "Mozilla/5.0",
                status,
                "HTTP/1.1",
                100L,
                200L,
                50L,
                "TLSv1.3",
                path);
    }
}
