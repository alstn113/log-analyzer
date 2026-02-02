package io.github.alstn113.assignment.application;

import io.github.alstn113.assignment.application.dto.GetLogAnalysisResultQuery;
import io.github.alstn113.assignment.application.dto.LogAnalysisResultDto;
import io.github.alstn113.assignment.application.dto.SubmitAnalysisResult;
import io.github.alstn113.assignment.domain.analysis.Analysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisProcessor analysisProcessor;
    private final AnalysisRepository analysisRepository;
    private final FileStorage fileStorage;

    /**
     * 로그 파일을 저장하고, 분석 작업을 비동기적으로 시작합니다.
     * 파일 키를 전달하여 분석 프로세서가 파일을 불러올 수 있도록 합니다.
     */
    public SubmitAnalysisResult analyzeLog(MultipartFile file) {
        String fileKey = fileStorage.save(file);
        Analysis analysis = savePendingAnalysis();

        analysisProcessor.process(analysis.getId(), fileKey);

        return new SubmitAnalysisResult(analysis.getId());
    }

    public LogAnalysisResultDto getResult(GetLogAnalysisResultQuery query) {
        // TODO:
        return null;
    }

    private Analysis savePendingAnalysis() {
        Analysis pending = Analysis.pending();
        return analysisRepository.save(pending);
    }
}
