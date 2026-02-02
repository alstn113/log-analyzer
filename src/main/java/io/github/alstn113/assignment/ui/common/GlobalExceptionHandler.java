package io.github.alstn113.assignment.ui.common;

import io.github.alstn113.assignment.domain.BaseException;
import io.github.alstn113.assignment.ui.common.response.ApiErrorDetail;
import io.github.alstn113.assignment.ui.common.response.ApiResponseDto;
import io.github.alstn113.assignment.ui.common.response.ErrorType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 사용자 정의 예외 처리
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

    // @Valid 유효성 검사 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        log.warn("MethodArgumentNotValidException : {}", e.getMessage(), e);

        List<ApiErrorDetail> errorDetails = e
                .getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ApiErrorDetail(
                        fieldError.getField(),
                        fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "유효하지 않은 값입니다."))
                .toList();

        ErrorType errorType = ErrorType.BAD_REQUEST;
        ApiResponseDto<Void> response = ApiResponseDto.error(errorType, errorDetails);

        return ResponseEntity.status(errorType.getStatus()).body(response);
    }

    // 파일 크기 초과 예외 처리
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException e) {
        log.warn("FileSizeExceeded: {}", e.getMessage(), e);

        ErrorType errorType = ErrorType.FILE_SIZE_EXCEEDED;
        ApiResponseDto<Void> response = ApiResponseDto.error(errorType);

        return ResponseEntity
                .status(errorType.getStatus())
                .body(response);
    }

    // 기타 예외 처리
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
