package com.example.boardTest.global.exception;

/**
 * 애플리케이션 전반에서 사용하는 커스텀 런타임 예외
 * ErrorCode를 함께 보관하여 상태 코드와 메시지를 일관되게 처리한다.
 */
public class CustomException extends RuntimeException {

    // 에러의 종류를 정의한 ErrorCode
    private final ErrorCode errorCode;

    /**
     * ErrorCode를 받아 RuntimeException으로 생성
     * @param errorCode 정의된 에러 코드 enum
     */
    public CustomException(ErrorCode errorCode) {
        // RuntimeException의 message로 ErrorCode의 메시지를 전달
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * GlobalExceptionHandler에서 ErrorCode를 꺼내기 위한 getter
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
