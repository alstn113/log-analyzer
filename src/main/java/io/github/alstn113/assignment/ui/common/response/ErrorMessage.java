package io.github.alstn113.assignment.ui.common.response;

public record ErrorMessage(
        String code,
        String message,
        Object data
) {

    public ErrorMessage(ErrorType errorType, Object data) {
        this(errorType.name(), errorType.getMessage(), data);
    }

    public ErrorMessage(ErrorType errorType) {
        this(errorType.name(), errorType.getMessage(), null);
    }
}
