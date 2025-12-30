package com.example.boardTest.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 애플리케이션에서 사용하는 에러 코드 정의 enum
 * HTTP 상태 코드 + 사용자에게 보여줄 메시지를 함께 관리
 */
public enum ErrorCode {

    // 400 Bad Request - 잘못된 입력값
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // 404 Not Found - 엔티티를 찾을 수 없음
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 데이터를 찾을 수 없습니다."),

    // 500 Internal Server Error - 서버 내부 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 401 Unauthorized - 인증 필요
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    // 403 Forbidden - 권한 없음
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    // HTTP 상태 코드
    private final HttpStatus status;

    // 클라이언트에 전달할 에러 메시지
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
