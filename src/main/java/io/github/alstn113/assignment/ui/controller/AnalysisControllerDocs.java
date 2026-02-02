package io.github.alstn113.assignment.ui.controller;

import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.application.dto.SubmitAnalysisResult;
import io.github.alstn113.assignment.ui.common.response.ApiResponseDto;
import io.github.alstn113.assignment.ui.common.validation.ValidCsv;
import io.github.alstn113.assignment.ui.controller.dto.GetLogAnalysisResultParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Analysis", description = "로그 분석 API")
public interface AnalysisControllerDocs {

    @Operation(summary = "로그 파일 업로드 및 분석", description = "로그 파일을 업로드하여 비동기 분석을 시작합니다. 분석 ID를 즉시 반환하며, 실제 분석은 백그라운드에서 진행됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "분석 요청 수락됨", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SubmitAnalysisResult.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 누락, 빈 파일 등)", content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    ResponseEntity<ApiResponseDto<SubmitAnalysisResult>> analyzeLog(
            @Parameter(description = "분석할 로그 파일 (csv)", required = true) @ValidCsv MultipartFile file);

    @Operation(summary = "로그 분석 결과 조회", description = "지정한 분석 ID의 결과 및 상태를 조회합니다. 분석이 완료되지 않은 경우 상태(status) 정보만 포함될 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 결과 조회함", content = @Content(schema = @Schema(implementation = LogAnalysisResultDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 분석 ID를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    ResponseEntity<ApiResponseDto<LogAnalysisResultDto>> getResult(
            @Parameter(description = "분석 ID", example = "1", required = true) @PathVariable Long analysisId,
            @ParameterObject @Valid GetLogAnalysisResultParams params);
}
