package com.study.grpc.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 회원 정보 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserUpdateRequest {

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    private String name;

    @Pattern(
        regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
        message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)"
    )
    private String phoneNumber;
}

