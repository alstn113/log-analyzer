package io.github.alstn113.assignment.domain.analysis;

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

}
