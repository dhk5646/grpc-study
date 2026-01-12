package com.study.grpc.user.dto;

import com.study.grpc.user.domain.User;
import com.study.grpc.user.domain.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 회원 응답 DTO
 *
 * Entity를 외부에 노출하지 않고 필요한 정보만 전달
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

