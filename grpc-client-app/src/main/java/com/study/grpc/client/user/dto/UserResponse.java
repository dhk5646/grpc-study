package com.study.grpc.client.user.dto;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private String status;
    private Long createdAt;
    private Long updatedAt;
}
