package io.github.alstn113.assignment.application.exception;

import io.github.alstn113.assignment.domain.BaseException;

public class AnalysisNotFoundException extends BaseException {

    public AnalysisNotFoundException(String message) {
        super(message);
    }

    public AnalysisNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
