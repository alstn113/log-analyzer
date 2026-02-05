package io.github.alstn113.assignment.infra.parser;

import io.github.alstn113.assignment.application.LogParser;
import io.github.alstn113.assignment.domain.analysis.vo.LogEntry;
import io.github.alstn113.assignment.domain.analysis.vo.ParsingErrors;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@Slf4j
class CsvLogStream implements LogParser.LogStream {

    private static final int MAX_ERROR_SAMPLES = 10;

    private final CSVParser parser;
    private final CsvToLogEntryMapper mapper;
    private final List<String> errorSamples = new ArrayList<>();
    private int errorCount = 0;

    public CsvLogStream(CSVParser parser, CsvToLogEntryMapper mapper) {
        this.parser = parser;
        this.mapper = mapper;
    }

    @Override
    public Stream<LogEntry> logEntries() {
        return StreamSupport.stream(parser.spliterator(), false)
                .map(this::parseRecordOrCollect)
                .filter(Objects::nonNull)
                .onClose(this::close);
    }

    @Override
    public ParsingErrors parsingErrors() {
        return new ParsingErrors(errorCount, new ArrayList<>(errorSamples));
    }

    @Override
    public void close() {
        try {
            parser.close();
        } catch (IOException e) {
            log.warn("CSV 파서 리소스 해제 중 오류 발생", e);
        }
    }

    private LogEntry parseRecordOrCollect(CSVRecord r) {
        try {
            return mapper.map(r);
        } catch (Exception e) {
            if (errorSamples.size() < MAX_ERROR_SAMPLES) {
                errorSamples.add("라인 %d: %s (예외: %s)".formatted(r.getRecordNumber(), r, e.getMessage()));
            }
            errorCount++;
            log.warn("로그 파싱 중 오류 발생 - 라인 {}: {}, 예외: {}", r.getRecordNumber(), r, e.getMessage());
            return null;
        }
    }
}
