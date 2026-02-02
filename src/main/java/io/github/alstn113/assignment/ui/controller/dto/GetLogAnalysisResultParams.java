package io.github.alstn113.assignment.ui.controller.dto;

import io.github.alstn113.assignment.application.dto.GetLogAnalysisResultQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.Range;

public record GetLogAnalysisResultParams(
        @Schema(description = "조회할 상위 경로 개수", example = "10", defaultValue = "10")
        @Range(min = 1, max = 100, message = "topPaths 값은 1에서 100 사이여야 합니다.")
        Integer topPaths,

        @Schema(description = "조회할 상위 상태 코드 개수", example = "10", defaultValue = "10")
        @Range(min = 1, max = 100, message = "topStatusCodes 값은 1에서 100 사이여야 합니다.")
        Integer topStatusCodes,

        @Schema(description = "조회할 상위 IP 개수", example = "10", defaultValue = "10")
        @Range(min = 1, max = 100, message = "topIps 값은 1에서 100 사이여야 합니다.")
        Integer topIps
) {

    public GetLogAnalysisResultParams(Integer topPaths, Integer topStatusCodes, Integer topIps) {
        this.topPaths = topPaths == null ? 10 : topPaths;
        this.topStatusCodes = topStatusCodes == null ? 10 : topStatusCodes;
        this.topIps = topIps == null ? 10 : topIps;
    }

    public GetLogAnalysisResultQuery toQuery(Long analysisId) {
        return new GetLogAnalysisResultQuery(analysisId, topPaths, topStatusCodes, topIps);
    }
}
