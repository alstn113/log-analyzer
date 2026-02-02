package io.github.alstn113.assignment.ui.controller;

import io.github.alstn113.assignment.application.AnalysisService;
import io.github.alstn113.assignment.application.dto.GetLogAnalysisResultQuery;
import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.application.dto.SubmitAnalysisResult;
import io.github.alstn113.assignment.ui.common.response.ApiResponseDto;
import io.github.alstn113.assignment.ui.controller.dto.GetLogAnalysisResultParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
public class AnalysisController implements AnalysisControllerDocs {

    private final AnalysisService analysisService;

    @PostMapping(value = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<SubmitAnalysisResult>> analyzeLog(
            @RequestPart("file") MultipartFile file
    ) {
        SubmitAnalysisResult result = analysisService.analyzeLog(file);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponseDto.success(result));
    }

    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<ApiResponseDto<LogAnalysisResultDto>> getResult(
            @PathVariable Long analysisId,
            @ModelAttribute GetLogAnalysisResultParams params
    ) {
        GetLogAnalysisResultQuery query = params.toQuery(analysisId);
        LogAnalysisResultDto result = analysisService.getResult(query);

        return ResponseEntity.ok(ApiResponseDto.success(result));
    }
}
