package com.study.grpc.user.service;

import com.study.grpc.user.domain.User;
import com.study.grpc.user.domain.UserStatus;
import com.study.grpc.user.dto.UserCreateRequest;
import com.study.grpc.user.dto.UserResponse;
import com.study.grpc.user.dto.UserUpdateRequest;
import com.study.grpc.user.exception.DuplicateEmailException;
import com.study.grpc.user.exception.InvalidUserStatusException;
import com.study.grpc.user.exception.UserNotFoundException;
import com.study.grpc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service
 *
 * 회원 관련 비즈니스 로직을 처리하는 서비스 계층
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원 가입
     *
     * @param request 회원 가입 요청 DTO
     * @return 생성된 회원 정보
     * @throws DuplicateEmailException 이메일이 이미 존재하는 경우
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("회원 가입 시도: email={}", request.getEmail());

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("이메일 중복: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }

        // Entity 생성 및 저장
        User user = request.toEntity();
        User savedUser = userRepository.save(user);

        log.info("회원 가입 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return UserResponse.from(savedUser);
    }

    /**
     * ID로 회원 조회
     *
     * @param id 회원 ID
     * @return 회원 정보
     * @throws UserNotFoundException 회원을 찾을 수 없는 경우
     */
    public UserResponse getUserById(Long id) {
        log.debug("회원 조회: id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return UserResponse.from(user);
    }

    /**
     * 전체 회원 목록 조회
     *
     * @return 회원 목록
     */
    public List<UserResponse> getAllUsers() {
        log.debug("전체 회원 목록 조회");

        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 활성 회원 목록 조회
     *
     * @return 활성 회원 목록
     */
    public List<UserResponse> getActiveUsers() {
        log.debug("활성 회원 목록 조회");

        return userRepository.findAllActiveUsers().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 이름으로 회원 검색
     *
     * @param name 검색할 이름
     * @return 검색된 회원 목록
     */
    public List<UserResponse> searchUsersByName(String name) {
        log.debug("회원 검색: name={}", name);

        return userRepository.findByNameContaining(name).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 회원 정보 수정
     *
     * @param id 회원 ID
     * @param request 수정 요청 DTO
     * @return 수정된 회원 정보
     * @throws UserNotFoundException 회원을 찾을 수 없는 경우
     * @throws InvalidUserStatusException 삭제된 회원인 경우
     */
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("회원 정보 수정: id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // 삭제된 회원은 수정 불가
        if (user.getStatus() == UserStatus.DELETED) {
            throw new InvalidUserStatusException("삭제된 회원은 수정할 수 없습니다.");
        }

        user.updateInfo(request.getName(), request.getPhoneNumber());

        log.info("회원 정보 수정 완료: id={}", id);
        return UserResponse.from(user);
    }

    /**
     * 회원 삭제 (소프트 삭제)
     *
     * @param id 회원 ID
     * @throws UserNotFoundException 회원을 찾을 수 없는 경우
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("회원 삭제: id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.delete();

        log.info("회원 삭제 완료: id={}", id);
    }

    /**
     * 회원 상태 변경
     *
     * @param id 회원 ID
     * @param status 변경할 상태
     * @return 수정된 회원 정보
     * @throws UserNotFoundException 회원을 찾을 수 없는 경우
     */
    @Transactional
    public UserResponse updateUserStatus(Long id, UserStatus status) {
        log.info("회원 상태 변경: id={}, status={}", id, status);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.updateStatus(status);

        log.info("회원 상태 변경 완료: id={}, status={}", id, status);
        return UserResponse.from(user);
    }
}

