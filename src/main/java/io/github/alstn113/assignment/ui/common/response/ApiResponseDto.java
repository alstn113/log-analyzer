package io.github.alstn113.assignment.ui.common.response;

import java.util.List;

public record ApiResponseDto<T>(
        boolean success,
        T data,
        ErrorMessage error
) {

    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(true, data, null);
    }

    public static <T> ApiResponseDto<T> error(ErrorType errorType) {
        return new ApiResponseDto<>(false, null, new ErrorMessage(errorType));
    }

    public static <T> ApiResponseDto<T> error(ErrorType errorType, Object errorData) {
        return new ApiResponseDto<>(false, null, new ErrorMessage(errorType, errorData));
    }

    public static <T> ApiResponseDto<T> error(ErrorType errorType, List<ApiErrorDetail> details) {
        return new ApiResponseDto<>(false, null, new ErrorMessage(errorType, details));
    }
}