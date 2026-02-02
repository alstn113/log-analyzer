package io.github.alstn113.assignment.infra.client.exception;

import io.github.alstn113.assignment.domain.BaseException;

public class IpInfoNonRetryableException extends BaseException {

    public IpInfoNonRetryableException(String message) {
        super(message);
    }
}
