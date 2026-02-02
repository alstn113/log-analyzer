package io.github.alstn113.assignment.infra.persistence;

import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto.ParsingErrorSummary;
import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto.Summary;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.PathCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.StatusCodeCount;
import java.util.Comparator;
import java.util.List;

public class AnalysisEntityMapper {

    private AnalysisEntityMapper() {
    }

    public static LogAnalysisResultDto toGetAnalysisResult(
            AnalysisEntity entity,
            int topPaths,
            int topStatusCodes,
            int topIps
    ) {
        return switch (entity.status()) {
            case PENDING, PROCESSING -> LogAnalysisResultDto.processing(entity.id(), entity.status());
            case FAILED -> LogAnalysisResultDto.failed(entity.id(), entity.errorMessage());
            case COMPLETED -> toCompletedResult(entity, topPaths, topStatusCodes, topIps);
        };
    }

    private static LogAnalysisResultDto toCompletedResult(
            AnalysisEntity entity,
            int topPaths,
            int topStatusCodes,
            int topIps
    ) {
        long totalLines = entity.totalRequests() + entity.parsingErrors().errorCount();
        Summary summary = LogAnalysisResultDto.Summary
                .from(totalLines, entity.totalRequests(), entity.statusCodeDistribution());

        List<LogAnalysisResultDto.PathCount> limitedTopPaths = entity.topPaths().stream()
                .sorted(Comparator.comparingLong(PathCount::count).reversed())
                .limit(topPaths)
                .map(LogAnalysisResultDto.PathCount::from)
                .toList();

        List<LogAnalysisResultDto.StatusCodeCount> limitedTopStatusCodes = entity.topStatusCodes().stream()
                .sorted(Comparator.comparingLong(StatusCodeCount::count).reversed())
                .limit(topStatusCodes)
                .map(LogAnalysisResultDto.StatusCodeCount::from)
                .toList();

        List<LogAnalysisResultDto.IpCount> limitedTopIps = entity.topIps().stream()
                .sorted(Comparator.comparingLong(IpCount::count).reversed())
                .limit(topIps)
                .map(LogAnalysisResultDto.IpCount::from)
                .toList();

        ParsingErrorSummary errorSummary = ParsingErrorSummary.of(
                entity.parsingErrors().errorCount(),
                totalLines
        );

        return new LogAnalysisResultDto(
                entity.id(),
                entity.status(),
                null,
                summary,
                errorSummary,
                limitedTopPaths,
                limitedTopStatusCodes,
                limitedTopIps
        );
    }
}
