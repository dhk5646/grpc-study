package com.study.grpc.client.user.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBatchCreateRequest {
    private List<UserCreateRequest> users;
}
