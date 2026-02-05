package io.github.alstn113.assignment.infra.parser;

import io.github.alstn113.assignment.application.LogParser;
import io.github.alstn113.assignment.application.exception.FileProcessingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvLogParser implements LogParser {

    private final CsvToLogEntryMapper mapper;

    @Override
    public LogStream parse(File file) {
        log.info("CSV 로그 파싱 시작 - file: {}", file.getName());

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader() // 값을 넣으면 헤더를 직접 지정 가능
                .setSkipHeaderRecord(true) // 첫 번째 행을 헤더로 건너뜀
                .setIgnoreEmptyLines(true) // 빈 줄 무시
                .setTrim(true)
                .get();

        CSVParser parser = null;
        try {
            InputStream bomStripped = BOMInputStream.builder()
                    .setFile(file)
                    .setByteOrderMarks(ByteOrderMark.UTF_8)
                    .setInclude(false)
                    .get();
            BufferedReader reader = new BufferedReader(new InputStreamReader(bomStripped, StandardCharsets.UTF_8));

            // 1. Parser 생성 (여기서 헤더 읽기를 시도할 수 있음)
            parser = CSVParser.builder()
                    .setReader(reader)
                    .setFormat(format)
                    .get();

            // 2. 헤더 검증
            validateHeader(parser.getHeaderNames());

            // 3. 성공 시 스트림 반환 (리소스 소유권 이전)
            return new CsvLogStream(parser, mapper);

        } catch (Exception e) {
            // 실패 시 생성된 리소스 정리
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException ex) {
                    log.warn("파서 종료 중 오류", ex);
                }
            }
            throw new FileProcessingException("로그 파싱 중 문제가 생겼습니다.", e);
        }
    }

    private void validateHeader(List<String> actualHeaders) {
        List<String> expectedHeaders = Arrays.asList(CsvToLogEntryMapper.LogSchema.getHeaders());
        if (!actualHeaders.equals(expectedHeaders)) {
            log.warn("CSV 파일 헤더 불일치. 예상: {}, 실제: {}", expectedHeaders, actualHeaders);
            throw new FileProcessingException("CSV 파일의 헤더가 기대값과 다릅니다");
        }
    }
}
