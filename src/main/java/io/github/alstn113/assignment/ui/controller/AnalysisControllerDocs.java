package io.github.alstn113.assignment.ui.controller;

import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.application.dto.SubmitAnalysisResult;
import io.github.alstn113.assignment.ui.common.response.ApiResponseDto;
import io.github.alstn113.assignment.ui.common.validation.ValidCsv;
import io.github.alstn113.assignment.ui.controller.dto.GetLogAnalysisResultParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
            @ApiResponse(responseCode = "202", description = "분석 요청 수락됨", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "성공 예시", value = """
                    {
                      "success": true,
                      "data": {
                        "analysisId": 1
                      },
                      "error": null
                    }
                    """), schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 누락, 빈 파일 등)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "검증 오류 예시", value = """
                            {
                              "success": false,
                              "data": null,
                              "error": {
                                "code": "BAD_REQUEST",
                                "message": "잘못된 요청입니다.",
                                "data": [
                                  {
                                    "field": "file",
                                    "message": "파일이 비어있거나 CSV 형식이 아닙니다."
                                  }
                                ]
                              }
                            }
                            """)
            }, schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "413", description = "파일 크기 초과", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "파일 크기 초과 예시", value = """
                            {
                                "success": false,
                                "data": null,
                                "error": {
                                    "code": "FILE_SIZE_EXCEEDED",
                                    "message": "파일 크기가 최대 허용 크기를 초과했습니다.",
                                    "data": null
                                }
                            }
                            """)
            }, schema = @Schema(implementation = ApiResponseDto.class)))
    })
    ResponseEntity<ApiResponseDto<SubmitAnalysisResult>> analyzeLog(
            @Parameter(description = "분석할 로그 파일 (csv)", required = true) @ValidCsv MultipartFile file);

    @Operation(summary = "로그 분석 결과 조회", description = "지정한 분석 ID의 결과 및 상태를 조회합니다. 분석이 완료되지 않은 경우 상태(status) 정보만 포함될 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 결과 조회함", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "분석 완료 예시", value = """
                            {
                              "success": true,
                              "data": {
                                "analysisId": 1,
                                "status": "COMPLETED",
                                "summary": {
                                  "totalCount": 182267,
                                  "validCount": 182265,
                                  "statusCodeDistribution": {
                                    "success2xx": 63,
                                    "redirect3xx": 2.7,
                                    "clientError4xx": 32.8,
                                    "serverError5xx": 1.4
                                  }
                                },
                                "parsingErrors": {
                                  "errorCount": 2,
                                  "errorRate": 0
                                },
                                "topPaths": [ { "path": "/", "count": 15240 } ],
                                "topStatusCodes": [ { "statusCode": 200, "count": 114352 } ],
                                "topIps": [
                                  {
                                    "ip": "120.242.23.238",
                                    "count": 20916,
                                    "ipInfo": {
                                      "country": "CN",
                                      "region": "Shanghai",
                                      "city": "Shanghai",
                                      "org": "AS9808 China Mobile Communications Group Co., Ltd."
                                    }
                                  }
                                ]
                              },
                              "error": null
                            }
                            """),
                    @ExampleObject(name = "분석 실패 예시", value = """
                            {
                              "success": true,
                              "data": {
                                "analysisId": 2,
                                "status": "FAILED",
                                "errorMessage": "CSV 파일의 헤더가 기대값과 다릅니다"
                              },
                              "error": null
                            }
                            """)
            }, schema = @Schema(implementation = LogAnalysisResultDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "범위 초과 예시", value = """
                    {
                      "success": false,
                      "data": null,
                      "error": {
                        "code": "BAD_REQUEST",
                        "message": "잘못된 요청입니다.",
                        "data": [
                          {
                            "field": "topPaths",
                            "message": "topPaths 값은 1에서 100 사이여야 합니다."
                          }
                        ]
                      }
                    }
                    """), schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 분석 ID를 찾을 수 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "미존재 ID 예시", value = """
                    {
                      "success": false,
                      "data": null,
                      "error": {
                        "code": "ANALYSIS_NOT_FOUND",
                        "message": "분석 결과를 찾을 수 없습니다.",
                        "data": null
                      }
                    }
                    """), schema = @Schema(implementation = ApiResponseDto.class)))
    })
    ResponseEntity<ApiResponseDto<LogAnalysisResultDto>> getResult(
            @Parameter(description = "분석 ID", example = "1", required = true) @PathVariable Long analysisId,
            @ParameterObject @Valid GetLogAnalysisResultParams params);
}
