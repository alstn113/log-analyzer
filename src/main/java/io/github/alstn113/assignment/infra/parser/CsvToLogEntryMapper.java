package io.github.alstn113.assignment.infra.parser;

import io.github.alstn113.assignment.domain.analysis.vo.LogEntry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CsvToLogEntryMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("M/d/yyyy, h:mm:ss.SSS a", Locale.ENGLISH);

    public LogEntry map(CSVRecord record) {
        return new LogEntry(
                parseDateTime(record.get(LogSchema.TIME_GENERATED.getHeader())),
                record.get(LogSchema.CLIENT_IP.getHeader()),
                record.get(LogSchema.HTTP_METHOD.getHeader()),
                record.get(LogSchema.REQUEST_URI.getHeader()),
                record.get(LogSchema.USER_AGENT.getHeader()),
                Integer.parseInt(record.get(LogSchema.HTTP_STATUS.getHeader())),
                record.get(LogSchema.HTTP_VERSION.getHeader()),
                Long.parseLong(record.get(LogSchema.RECEIVED_BYTES.getHeader())),
                Long.parseLong(record.get(LogSchema.SENT_BYTES.getHeader())),
                Math.round(Double.parseDouble(record.get(LogSchema.CLIENT_RESPONSE_TIME.getHeader())) * 1000), // ms로 변환
                record.get(LogSchema.SSL_PROTOCOL.getHeader()),
                record.get(LogSchema.ORIGINAL_REQUEST_URI_WITH_ARGS.getHeader()));
    }

    private LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value, DATE_FORMATTER);
    }

    @Getter
    @RequiredArgsConstructor
    public enum LogSchema {
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

        public String getHeader() {
            return header;
        }

        public static String[] getHeaders() {
            return java.util.Arrays.stream(values())
                    .map(LogSchema::getHeader)
                    .toArray(String[]::new);
        }
    }
}
