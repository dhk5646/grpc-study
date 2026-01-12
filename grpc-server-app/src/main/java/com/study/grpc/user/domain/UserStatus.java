package com.study.grpc.user.domain;

/**
 * 회원 상태 Enum
 */
public enum UserStatus {
    /**
     * 활성 상태
     */
    ACTIVE,

    /**
     * 비활성 상태
     */
    INACTIVE,

    /**
     * 삭제됨
     */
    DELETED,

    /**
     * 정지됨
     */
    SUSPENDED
}

