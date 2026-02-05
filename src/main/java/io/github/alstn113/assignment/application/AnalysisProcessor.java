package io.github.alstn113.assignment.application;

import io.github.alstn113.assignment.application.LogParser.LogStream;
import io.github.alstn113.assignment.application.exception.AnalysisNotFoundException;
import io.github.alstn113.assignment.application.exception.FileProcessingException;
import io.github.alstn113.assignment.domain.analysis.Analysis;
import io.github.alstn113.assignment.domain.analysis.Analysis.AnalysisResult;
import io.github.alstn113.assignment.domain.analysis.service.LogAggregator;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpCount;
import io.github.alstn113.assignment.domain.analysis.vo.ParsingErrors;
import java.io.File;
import java.util.List;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisProcessor {

    private final IpEnrichmentService ipEnrichmentService;
    private final AnalysisRepository analysisRepository;
    private final FileStorage fileStorage;
    private final LogParser logParser;

    /**
     * 파일 키로 저장한 임시 파일을 불러와서 분석합니다.
     * 비동기 방식으로 로그 분석을 처리합니다. pending -> processing -> completed || failed
     */
    @Async("analysisTaskExecutor")
    public void process(Long analysisId, String fileKey) {
        log.info("로그 분석 시작 - analysisId: {}", analysisId);

        try {
            updateStatus(analysisId, Analysis::processing);
            AnalysisResult result = execute(fileKey);
            updateStatus(analysisId, analysis -> analysis.complete(result));

            log.info("로그 분석 완료 - analysisId: {}, 총 요청수: {}", analysisId, result.stats().totalRequests());
        } catch (FileProcessingException e) {
            updateStatus(analysisId, analysis -> analysis.fail(e.getMessage()));

            log.warn("로그 분석 실패 (파일 처리 오류) - analysisId: {}, message: {}", analysisId, e.getMessage());
        } catch (Exception e) {
            String message = "알 수 없는 오류로 인해 분석에 실패했습니다";
            updateStatus(analysisId, analysis -> analysis.fail(message));

            log.warn("로그 분석 실패 (알 수 없는 오류) - analysisId: {}", analysisId, e);
        } finally {
            fileStorage.delete(fileKey);
        }
    }

    private AnalysisResult execute(String fileKey) {
        File file = fileStorage.load(fileKey);

        try (LogStream logStream = logParser.parse(file)) {
            long start = System.currentTimeMillis();
            LogStatistics stats = LogAggregator.aggregate(logStream.logEntries());
            long duration = System.currentTimeMillis() - start;
            log.info("CSV 로그 분석 시간: {} ms", duration);

            List<IpCount> enrichedTopIps = ipEnrichmentService.enrich(stats.topIps());
            ParsingErrors parsingErrors = logStream.parsingErrors();

            return new AnalysisResult(stats, enrichedTopIps, parsingErrors);
        }
    }

    private void updateStatus(Long analysisId, UnaryOperator<Analysis> stateTransition) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> {
                    String message = "Analysis (id: %d) 가 존재하지 않습니다.".formatted(analysisId);
                    return new AnalysisNotFoundException(message);
                });

        Analysis nextState = stateTransition.apply(analysis);
        analysisRepository.update(nextState);
    }
}
