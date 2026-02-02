package io.github.alstn113.assignment.ui.common;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        if (ex instanceof RuntimeException) {
            log.warn("BaseException in async method {}: {}", method.getName(), ex.getMessage(), ex);
        } else {
            log.error("Exception in async method {}: {}", method.getName(), ex.getMessage(), ex);
        }
    }
}
