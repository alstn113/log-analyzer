package io.github.alstn113.assignment.infra.persistence;

import io.github.alstn113.assignment.application.AnalysisRepository;
import io.github.alstn113.assignment.application.dto.GetLogAnalysisResultQuery;
import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.domain.analysis.Analysis;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAnalysisRepository implements AnalysisRepository {

    private final Map<Long, AnalysisEntity> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Analysis save(Analysis analysis) {
        Long id = idGenerator.getAndIncrement();
        Analysis savedAnalysis = saveAnalysis(analysis, id);

        AnalysisEntity entity = AnalysisEntity.from(savedAnalysis);
        storage.put(id, entity);

        return savedAnalysis;
    }

    @Override
    public void update(Analysis analysis) {
        Long id = analysis.getId();
        Analysis updated = saveAnalysis(analysis, id);

        AnalysisEntity entity = AnalysisEntity.from(updated);
        storage.put(id, entity);
    }

    @Override
    public Optional<Analysis> findById(Long id) {
        return Optional.ofNullable(storage.get(id))
                .map(AnalysisEntity::toDomain);
    }

    @Override
    public Optional<LogAnalysisResultDto> getLogAnalysisResult(GetLogAnalysisResultQuery query) {
        return Optional.ofNullable(storage.get(query.analysisId()))
                .map(entity -> AnalysisEntityMapper.toGetAnalysisResult(
                        entity,
                        query.topPaths(),
                        query.topStatusCodes(),
                        query.topIps()
                ));
    }

    private static Analysis saveAnalysis(Analysis analysis, Long id) {
        return Analysis.builder()
                .id(id)
                .status(analysis.getStatus())
                .errorMessage(analysis.getErrorMessage())
                .createdAt(analysis.getCreatedAt())
                .finishedAt(analysis.getFinishedAt())
                .totalRequests(analysis.getTotalRequests())
                .statusCodeDistribution(analysis.getStatusCodeDistribution())
                .topPaths(analysis.getTopPaths())
                .topStatusCodes(analysis.getTopStatusCodes())
                .topIps(analysis.getTopIps())
                .parsingErrors(analysis.getParsingErrors())
                .build();
    }
}
