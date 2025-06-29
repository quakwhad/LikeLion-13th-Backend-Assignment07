package com.likelion.basecode.common.template;

import com.likelion.basecode.common.error.ErrorCode;
import com.likelion.basecode.common.error.SuccessCode;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ApiResTemplate<T> {

    private final int code;
    private final String message;
    private T data;

    public static ApiResTemplate successWithNoContent(SuccessCode successCode) {
        return new ApiResTemplate<>(successCode.getHttpStatusCode(), successCode.getMessage());
    }

    public static <T> ApiResTemplate<T> successResponse(SuccessCode successCode, T data) {
        return new ApiResTemplate<>(successCode.getHttpStatusCode(), successCode.getMessage(), data);
    }

    public static ApiResTemplate errorResponse(ErrorCode errorCode, String customMessage) {
        return new ApiResTemplate<>(errorCode.getHttpStatusCode(), customMessage);
    }
}

