package com.study.grpc.user.exception;

/**
 * 유효하지 않은 사용자 상태일 때 발생하는 예외
 */
public class InvalidUserStatusException extends RuntimeException {

    public InvalidUserStatusException(String message) {
        super(message);
    }

    public InvalidUserStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}

