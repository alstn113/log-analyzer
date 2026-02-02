package io.github.alstn113.assignment.domain.analysis;

import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.PathCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.StatusCodeCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.StatusCodeStats;
import io.github.alstn113.assignment.domain.analysis.vo.ParsingErrors;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Analysis {

    private final Long id;
    private final AnalysisStatus status;
    private final String errorMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime finishedAt;
    private final long totalRequests;
    private final StatusCodeStats statusCodeDistribution;
    private final List<PathCount> topPaths;
    private final List<StatusCodeCount> topStatusCodes;
    private final List<IpCount> topIps;
    private final ParsingErrors parsingErrors;

    public static Analysis pending() {
        return Analysis.builder()
                .status(AnalysisStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Analysis processing() {
        return Analysis.builder()
                .id(this.id)
                .status(AnalysisStatus.PROCESSING)
                .createdAt(this.createdAt)
                .build();
    }

    public Analysis complete(AnalysisResult result) {
        return Analysis.builder()
                .id(this.id)
                .status(AnalysisStatus.COMPLETED)
                .createdAt(this.createdAt)
                .finishedAt(LocalDateTime.now())
                .totalRequests(result.stats().totalRequests())
                .statusCodeDistribution(result.stats().statusCodeDistribution())
                .topPaths(result.stats().topPaths())
                .topStatusCodes(result.stats().topStatusCodes())
                .topIps(result.enrichedIps())
                .parsingErrors(parsingErrors)
                .build();
    }

    public Analysis fail(String errorMessage) {
        return Analysis.builder()
                .id(this.id)
                .status(AnalysisStatus.FAILED)
                .errorMessage(errorMessage)
                .createdAt(this.createdAt)
                .finishedAt(LocalDateTime.now())
                .build();
    }

    public record AnalysisResult(
            LogStatistics stats,
            List<IpCount> enrichedIps,
            ParsingErrors parsingErrors
    ) {
    }
}
