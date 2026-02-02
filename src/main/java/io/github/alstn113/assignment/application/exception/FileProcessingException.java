package io.github.alstn113.assignment.application.exception;

import io.github.alstn113.assignment.domain.BaseException;

public class FileProcessingException extends BaseException {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
