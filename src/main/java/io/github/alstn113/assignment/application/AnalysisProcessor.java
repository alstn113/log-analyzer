package io.github.alstn113.assignment.application;

import io.github.alstn113.assignment.application.LogParser.ParseResult;
import io.github.alstn113.assignment.application.exception.AnalysisNotFoundException;
import io.github.alstn113.assignment.application.exception.FileProcessingException;
import io.github.alstn113.assignment.domain.analysis.Analysis;
import io.github.alstn113.assignment.domain.analysis.Analysis.AnalysisResult;
import io.github.alstn113.assignment.domain.analysis.service.LogAggregator;
import io.github.alstn113.assignment.domain.analysis.vo.LogEntry;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpCount;
import io.github.alstn113.assignment.domain.analysis.vo.ParsingErrors;
import java.io.File;
import java.util.List;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalysisProcessor {

    private final IpEnrichmentService ipEnrichmentService;
    private final AnalysisRepository analysisRepository;
    private final FileStorage fileStorage;
    private final LogParser logParser;

    /**
     * 비동기 방식으로 로그 분석을 처리합니다.
     * pending -> processing -> completed || failed
     */
    @Async("analysisTaskExecutor")
    public void process(Long analysisId, File file) {
        try {
            updateStatus(analysisId, Analysis::processing);
            AnalysisResult result = execute(file);
            updateStatus(analysisId, analysis -> analysis.complete(result));
        } catch (FileProcessingException e) {
            updateStatus(analysisId, analysis -> analysis.fail(e.getMessage()));
        } catch (Exception e) {
            String message = "알 수 없는 오류로 인해 분석에 실패했습니다";
            updateStatus(analysisId, analysis -> analysis.fail(message));
        } finally {
            fileStorage.delete(file);
        }
    }

    private AnalysisResult execute(File file) {
        ParseResult parseResult = logParser.parse(file);
        List<LogEntry> logEntries = parseResult.logEntries();
        ParsingErrors parsingErrors = parseResult.parsingErrors();

        LogStatistics stats = LogAggregator.aggregate(logEntries);
        List<IpCount> enrichedTopIps = ipEnrichmentService.enrich(stats.topIps());

        return new AnalysisResult(stats, enrichedTopIps, parsingErrors);
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
