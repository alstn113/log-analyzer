package io.github.alstn113.assignment.ui.controller;

import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.application.dto.SubmitAnalysisResult;
import io.github.alstn113.assignment.ui.common.response.ApiResponseDto;
import io.github.alstn113.assignment.ui.controller.dto.GetLogAnalysisResultParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Analysis", description = "로그 분석 API")
public interface AnalysisControllerDocs {

    @Operation(summary = "로그 파일 업로드 및 분석")
    ResponseEntity<ApiResponseDto<SubmitAnalysisResult>> analyzeLog(
            MultipartFile file
    );

    @Operation(summary = "로그 분석 결과 조회")
    ResponseEntity<ApiResponseDto<LogAnalysisResultDto>> getResult(
            Long analysisId,
            GetLogAnalysisResultParams params
    );
}
