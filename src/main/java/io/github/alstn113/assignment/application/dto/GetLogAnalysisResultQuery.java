package io.github.alstn113.assignment.application.dto;

public record GetLogAnalysisResultQuery(
        Long analysisId,
        Integer topPaths,
        Integer topStatusCodes,
        Integer topIps
) {
}
