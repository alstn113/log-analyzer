package io.github.alstn113.assignment.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alstn113.assignment.application.dto.GetLogAnalysisResultQuery;
import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.domain.analysis.Analysis;
import io.github.alstn113.assignment.domain.analysis.AnalysisStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryAnalysisRepositoryTest {

    private InMemoryAnalysisRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAnalysisRepository();
    }

    @Test
    @DisplayName("새로운 분석 정보를 저장하면 ID가 생성되고 저장된다")
    void saveNewAnalysis() {
        // given
        Analysis analysis = Analysis.pending();

        // when
        Analysis saved = repository.save(analysis);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(AnalysisStatus.PENDING);

        Optional<Analysis> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("기존 정보를 업데이트하면 내용이 변경된다")
    void updateAnalysis() {
        // given
        Analysis saved = repository.save(Analysis.pending());
        Analysis processing = saved.processing();

        // when
        repository.update(processing);

        // then
        Analysis found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(AnalysisStatus.PROCESSING);
    }

    @Test
    @DisplayName("분석 결과 조회를 위한 DTO 변환이 정상적으로 수행된다")
    void getLogAnalysisResult() {
        // given
        Analysis saved = repository.save(Analysis.pending());
        GetLogAnalysisResultQuery query = new GetLogAnalysisResultQuery(saved.getId(), 10, 10, 10);

        // when
        Optional<LogAnalysisResultDto> result = repository.getLogAnalysisResult(query);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().analysisId()).isEqualTo(saved.getId());
        assertThat(result.get().status()).isEqualTo(AnalysisStatus.PENDING);
    }
}
