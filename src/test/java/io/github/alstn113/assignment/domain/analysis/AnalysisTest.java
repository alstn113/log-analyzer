package io.github.alstn113.assignment.domain.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alstn113.assignment.domain.analysis.Analysis.AnalysisResult;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics;
import io.github.alstn113.assignment.domain.analysis.vo.ParsingErrors;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnalysisTest {

    @Test
    @DisplayName("Analysis 초기 상태는 PENDING 이다")
    void initStatusIsPending() {
        Analysis analysis = Analysis.pending();

        assertThat(analysis.getStatus()).isEqualTo(AnalysisStatus.PENDING);
        assertThat(analysis.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Analysis 가 완료되면 상태가 COMPLETED 로 변경되고 결과가 저장된다")
    void completeChangesStatusToCompleted() {
        // given
        Analysis analysis = Analysis.pending();
        LogStatistics stats = new LogStatistics(10L,
                new LogStatistics.StatusCodeStats(80.0, 10.0, 5.0, 5.0),
                List.of(), List.of(), List.of());
        ParsingErrors errors = new ParsingErrors(0, List.of());
        AnalysisResult result = new AnalysisResult(stats, List.of(), errors);

        // when
        Analysis completed = analysis.complete(result);

        // then
        assertThat(completed.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(completed.getTotalRequests()).isEqualTo(10L);
        assertThat(completed.getFinishedAt()).isAfterOrEqualTo(completed.getCreatedAt());
    }

    @Test
    @DisplayName("Analysis 가 실패하면 상태가 FAILED 로 변경되고 에러 메시지가 저장된다")
    void failChangesStatusToFailed() {
        // given
        Analysis analysis = Analysis.pending();
        String errorMessage = "테스트 에러";

        // when
        Analysis failed = analysis.fail(errorMessage);

        // then
        assertThat(failed.getStatus()).isEqualTo(AnalysisStatus.FAILED);
        assertThat(failed.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(failed.getFinishedAt()).isNotNull();
    }
}
