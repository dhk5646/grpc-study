package com.study.grpc.user.exception;

/**
 * 회원을 찾을 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("회원을 찾을 수 없습니다. ID: " + id);
    }

    public UserNotFoundException(String email) {
        super("회원을 찾을 수 없습니다. Email: " + email);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

