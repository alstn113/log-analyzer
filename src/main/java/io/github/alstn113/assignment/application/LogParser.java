package io.github.alstn113.assignment.application;

import io.github.alstn113.assignment.domain.analysis.vo.LogEntry;
import io.github.alstn113.assignment.domain.analysis.vo.ParsingErrors;
import java.io.File;
import java.util.List;

public interface LogParser {

    ParseResult parse(File file);

    record ParseResult(List<LogEntry> logEntries, ParsingErrors parsingErrors) {
    }
}
