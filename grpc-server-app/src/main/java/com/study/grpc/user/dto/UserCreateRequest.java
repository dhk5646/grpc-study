package com.study.grpc.user.dto;

import com.study.grpc.user.domain.User;
import com.study.grpc.user.domain.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 회원 가입 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserCreateRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 50, message = "이메일은 50자를 초과할 수 없습니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    private String name;

    @Pattern(
        regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
        message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)"
    )
    private String phoneNumber;

    /**
     * DTO를 Entity로 변환
     */
    public User toEntity() {
        return User.builder()
                .email(this.email)
                .password(this.password) // 실제로는 암호화 필요
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .status(UserStatus.ACTIVE)
                .build();
    }
}

