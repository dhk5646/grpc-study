package com.study.grpc.user.exception;

/**
 * 이메일이 이미 존재할 때 발생하는 예외
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("이미 존재하는 이메일입니다: " + email);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}

