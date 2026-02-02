package io.github.alstn113.assignment.domain.analysis.vo;

import java.util.List;

public record ParsingErrors(
        int errorCount,
        List<String> errorSamples
) {
}

