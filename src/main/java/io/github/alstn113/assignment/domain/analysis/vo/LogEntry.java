package io.github.alstn113.assignment.domain.analysis.vo;

import java.time.LocalDateTime;

public record LogEntry(
        LocalDateTime timeGenerated,
        String clientIp,
        String httpMethod,
        String requestUri,
        String userAgent,
        int httpStatus,
        String httpVersion,
        long receivedBytes,
        long sentBytes,
        long clientResponseTime,
        String sslProtocol,
        String originalRequestUriWithArgs
) {

    public boolean isSuccess() {
        return httpStatus >= 200 && httpStatus < 300;
    }

    public boolean isRedirect() {
        return httpStatus >= 300 && httpStatus < 400;
    }

    public boolean isClientError() {
        return httpStatus >= 400 && httpStatus < 500;
    }

    public boolean isServerError() {
        return httpStatus >= 500 && httpStatus < 600;
    }
}
