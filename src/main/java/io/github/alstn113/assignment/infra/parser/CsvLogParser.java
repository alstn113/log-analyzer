package io.github.alstn113.assignment.infra.parser;

import io.github.alstn113.assignment.application.LogParser;
import io.github.alstn113.assignment.application.exception.FileProcessingException;
import io.github.alstn113.assignment.domain.analysis.vo.LogEntry;
import io.github.alstn113.assignment.domain.analysis.vo.ParsingErrors;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Component
public class CsvLogParser implements LogParser {

    private static final int MAX_ERROR_SAMPLES = 10;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("M/d/yyyy, h:mm:ss.SSS a", Locale.ENGLISH);

    @Override
    public LogParser.ParseResult parse(File file) {
        StopWatch stopWatch = new StopWatch("CSV Log Parsing");
        stopWatch.start();

        List<LogEntry> entries = new ArrayList<>();
        List<String> errorSamples = new ArrayList<>();
        int errorCount = 0;

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader() // 값을 넣으면 헤더를 직접 지정 가능
                .setSkipHeaderRecord(true) // 첫 번째 행을 헤더로 건너뜀
                .setIgnoreEmptyLines(true) // 빈 줄 무시
                .setTrim(true)
                .get();

        try (
                InputStream bomStripped = BOMInputStream.builder()
                        .setFile(file)
                        .setByteOrderMarks(ByteOrderMark.UTF_8)
                        .setInclude(false)
                        .get();
                BufferedReader reader = new BufferedReader(new InputStreamReader(bomStripped, StandardCharsets.UTF_8));
                CSVParser parser = CSVParser.builder()
                        .setReader(reader)
                        .setFormat(format)
                        .get()
        ) {
            validateHeader(parser.getHeaderNames());

            for (CSVRecord r : parser) {
                LogEntry entry = parseRecordOrCollect(r, errorSamples);
                if (entry != null) {
                    entries.add(entry);
                } else {
                    errorCount++;
                }
            }
        } catch (IOException e) {
            throw new FileProcessingException("로그 파싱 중 문제가 생겼습니다.", e);
        }

        ParsingErrors parsingErrors = new ParsingErrors(errorCount, errorSamples);
        ParseResult result = new ParseResult(entries, parsingErrors);

        stopWatch.stop();
        log.info("CSV 로그 파싱 완료: {} 건 처리, {} 건 오류, 소요 시간 {} ms",
                entries.size(), errorCount, stopWatch.getTotalTimeMillis());

        return result;
    }

    private void validateHeader(List<String> actualHeaders) {
        List<String> expectedHeaders = Arrays.asList(LogSchema.getHeaders());
        if (!actualHeaders.equals(expectedHeaders)) {
            log.warn("CSV 파일 헤더 불일치. 예상: {}, 실제: {}", expectedHeaders, actualHeaders);
            throw new FileProcessingException("CSV 파일의 헤더가 기대값과 다릅니다");
        }
    }

    private LogEntry parseRecordOrCollect(CSVRecord r, List<String> errorSamples) {
        try {
            return parseRecord(r);
        } catch (Exception e) {
            if (errorSamples.size() < MAX_ERROR_SAMPLES) {
                errorSamples.add("라인 %d: %s (예외: %s)".formatted(r.getRecordNumber(), r, e.getMessage()));
            }
            log.warn("로그 파싱 중 오류 발생 - 라인 {}: {}, 예외: {}", r.getRecordNumber(), r, e.getMessage());
            return null;
        }
    }

    private LogEntry parseRecord(CSVRecord r) {
        return new LogEntry(
                parseDateTime(r.get(LogSchema.TIME_GENERATED.getHeader())),
                r.get(LogSchema.CLIENT_IP.getHeader()),
                r.get(LogSchema.HTTP_METHOD.getHeader()),
                r.get(LogSchema.REQUEST_URI.getHeader()),
                r.get(LogSchema.USER_AGENT.getHeader()),
                Integer.parseInt(r.get(LogSchema.HTTP_STATUS.getHeader())),
                r.get(LogSchema.HTTP_VERSION.getHeader()),
                Long.parseLong(r.get(LogSchema.RECEIVED_BYTES.getHeader())),
                Long.parseLong(r.get(LogSchema.SENT_BYTES.getHeader())),
                Math.round(Double.parseDouble(r.get(LogSchema.CLIENT_RESPONSE_TIME.getHeader())) * 1000), // ms로 변환
                r.get(LogSchema.SSL_PROTOCOL.getHeader()),
                r.get(LogSchema.ORIGINAL_REQUEST_URI_WITH_ARGS.getHeader()));
    }

    private LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value, DATE_FORMATTER);
    }

    @Getter
    @RequiredArgsConstructor
    private enum LogSchema {
        TIME_GENERATED("TimeGenerated [UTC]"),
        CLIENT_IP("ClientIp"),
        HTTP_METHOD("HttpMethod"),
        REQUEST_URI("RequestUri"),
        USER_AGENT("UserAgent"),
        HTTP_STATUS("HttpStatus"),
        HTTP_VERSION("HttpVersion"),
        RECEIVED_BYTES("ReceivedBytes"),
        SENT_BYTES("SentBytes"),
        CLIENT_RESPONSE_TIME("ClientResponseTime"),
        SSL_PROTOCOL("SslProtocol"),
        ORIGINAL_REQUEST_URI_WITH_ARGS("OriginalRequestUriWithArgs");

        private final String header;

        public static String[] getHeaders() {
            return Arrays.stream(values())
                    .map(LogSchema::getHeader)
                    .toArray(String[]::new);
        }
    }
}
