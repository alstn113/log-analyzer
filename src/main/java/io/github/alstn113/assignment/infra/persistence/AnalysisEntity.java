package io.github.alstn113.assignment.infra.persistence;

import io.github.alstn113.assignment.domain.analysis.Analysis;
import io.github.alstn113.assignment.domain.analysis.AnalysisStatus;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.PathCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.StatusCodeCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.StatusCodeStats;
import io.github.alstn113.assignment.domain.analysis.vo.ParsingErrors;
import java.time.LocalDateTime;
import java.util.List;

public record AnalysisEntity(
        Long id,
        AnalysisStatus status,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime finishedAt,
        long totalRequests,
        StatusCodeStats statusCodeDistribution,
        List<PathCount> topPaths,
        List<StatusCodeCount> topStatusCodes,
        List<IpCount> topIps,
        ParsingErrors parsingErrors
) {

    public static AnalysisEntity from(Analysis analysis) {
        return new AnalysisEntity(
                analysis.getId(),
                analysis.getStatus(),
                analysis.getErrorMessage(),
                analysis.getCreatedAt(),
                analysis.getFinishedAt(),
                analysis.getTotalRequests(),
                analysis.getStatusCodeDistribution(),
                analysis.getTopPaths(),
                analysis.getTopStatusCodes(),
                analysis.getTopIps(),
                analysis.getParsingErrors());
    }

    public Analysis toDomain() {
        return Analysis.builder()
                .id(id)
                .status(status)
                .errorMessage(errorMessage)
                .createdAt(createdAt)
                .finishedAt(finishedAt)
                .totalRequests(totalRequests)
                .statusCodeDistribution(statusCodeDistribution)
                .topPaths(topPaths)
                .topStatusCodes(topStatusCodes)
                .topIps(topIps)
                .parsingErrors(parsingErrors)
                .build();
    }
}