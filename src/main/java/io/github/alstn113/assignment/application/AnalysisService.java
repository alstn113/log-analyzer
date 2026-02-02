package io.github.alstn113.assignment.application;

import io.github.alstn113.assignment.application.dto.GetLogAnalysisResultQuery;
import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.application.dto.SubmitAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;

    public SubmitAnalysisResult analyzeLog(MultipartFile file) {}

    public LogAnalysisResultDto getAnalysis(GetLogAnalysisResultQuery query) {}
}
