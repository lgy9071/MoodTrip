package com.example.boardTest.global.exception;

import lombok.Builder;
import lombok.Getter;

/**
 * 클라이언트에게 반환되는 공통 에러 응답 DTO
 */
@Getter
public class ErrorResponse {

    // HTTP 상태 코드 값 (ex: 400, 404)
    private final int status;

    // 에러 메시지
    private final String message;

    /**
     * Builder 패턴을 사용한 생성자
     */
    @Builder
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * ErrorCode를 ErrorResponse로 변환하는 정적 팩토리 메서드
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .message(errorCode.getMessage())
                .build();
    }
}
