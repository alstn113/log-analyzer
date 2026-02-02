package io.github.alstn113.assignment.ui.controller.dto;

import io.github.alstn113.assignment.application.dto.GetLogAnalysisResultQuery;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;

@Getter
public class GetLogAnalysisResultParams {

    @Range(min = 1, max = 100, message = "topPaths 값은 1에서 100 사이여야 합니다.")
    private final Integer topPaths;
    @Range(min = 1, max = 100, message = "topStatusCodes 값은 1에서 100 사이여야 합니다.")
    private final Integer topStatusCodes;
    @Range(min = 1, max = 100, message = "topIps 값은 1에서 100 사이여야 합니다.")
    private final Integer topIps;

    public GetLogAnalysisResultParams(Integer topPaths, Integer topStatusCodes, Integer topIps) {
        this.topPaths = topPaths == null ? 10 : topPaths;
        this.topStatusCodes = topStatusCodes == null ? 10 : topStatusCodes;
        this.topIps = topIps == null ? 10 : topIps;
    }

    public GetLogAnalysisResultQuery toQuery(Long analysisId) {
        return new GetLogAnalysisResultQuery(analysisId, topPaths, topStatusCodes, topIps);
    }
}
