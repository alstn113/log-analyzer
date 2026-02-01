package io.github.alstn113.assignment.ui.common;

import io.github.alstn113.assignment.domain.BaseException;
import io.github.alstn113.assignment.ui.common.response.ApiResponseDto;
import io.github.alstn113.assignment.ui.common.response.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBaseException(BaseException e) {
        log.warn("BaseException: {}", e.getMessage(), e);

        ErrorType errorType = switch (e) {
            default -> ErrorType.BAD_REQUEST;
        };

        ApiResponseDto<Void> response = ApiResponseDto.error(errorType);
        return ResponseEntity
                .status(errorType.getStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);

        ErrorType errorType = ErrorType.INTERNAL_SERVER_ERROR;
        ApiResponseDto<Void> response = ApiResponseDto.error(errorType);

        return ResponseEntity
                .status(errorType.getStatus())
                .body(response);
    }
}
