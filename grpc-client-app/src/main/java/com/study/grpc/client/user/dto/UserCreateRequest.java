package com.study.grpc.client.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {
    private String email;
    private String name;
    private String phoneNumber;
}
