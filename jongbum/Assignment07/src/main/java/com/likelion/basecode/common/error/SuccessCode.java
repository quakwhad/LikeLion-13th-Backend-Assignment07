package com.likelion.basecode.common.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessCode {

    // 200
    GET_SUCCESS(HttpStatus.OK, "성공적으로 조회했습니다."),
    MEMBER_UPDATE_SUCCESS(HttpStatus.OK, "사용자가 성공적으로 수정되었습니다."),
    POST_UPDATE_SUCCESS(HttpStatus.OK, "글이 성공적으로 수정되었습니다."),
    MEMBER_DELETE_SUCCESS(HttpStatus.OK, "사용자가 성공적으로 삭제되었습니다."),
    POST_DELETE_SUCCESS(HttpStatus.OK, "글이 성공적으로 삭제되었습니다."),

    // 201
    MEMBER_SAVE_SUCCESS(HttpStatus.CREATED, "사용자가 성공적으로 생성되었습니다."),
    POST_SAVE_SUCCESS(HttpStatus.CREATED, "글이 성공적으로 생성되었습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
