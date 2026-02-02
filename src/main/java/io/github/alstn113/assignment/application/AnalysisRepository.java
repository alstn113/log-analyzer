package io.github.alstn113.assignment.application;

import io.github.alstn113.assignment.application.dto.GetLogAnalysisResultQuery;
import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.domain.analysis.Analysis;
import java.util.Optional;

public interface AnalysisRepository {

    Analysis save(Analysis analysis);

    void update(Analysis analysis);

    Optional<Analysis> findById(Long id);

    Optional<LogAnalysisResultDto> getLogAnalysisResult(GetLogAnalysisResultQuery query);
}
