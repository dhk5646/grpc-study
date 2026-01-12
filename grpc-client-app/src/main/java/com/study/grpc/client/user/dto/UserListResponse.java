package com.study.grpc.client.user.dto;
import lombok.*;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListResponse {
    private List<UserResponse> users;
    private Integer totalCount;
}
