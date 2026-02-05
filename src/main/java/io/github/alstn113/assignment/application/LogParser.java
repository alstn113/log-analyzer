package io.github.alstn113.assignment.application;

import io.github.alstn113.assignment.domain.analysis.vo.LogEntry;
import io.github.alstn113.assignment.domain.analysis.vo.ParsingErrors;
import java.io.File;
import java.util.stream.Stream;

public interface LogParser {

    LogStream parse(File file);

    interface LogStream extends AutoCloseable {
        Stream<LogEntry> logEntries();

        ParsingErrors parsingErrors();

        @Override
        void close();
    }
}
