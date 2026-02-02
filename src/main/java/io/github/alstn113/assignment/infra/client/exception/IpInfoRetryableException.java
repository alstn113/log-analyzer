package io.github.alstn113.assignment.infra.client.exception;

import io.github.alstn113.assignment.domain.BaseException;

public class IpInfoRetryableException extends BaseException {

    public IpInfoRetryableException(String message) {
        super(message);
    }
}
