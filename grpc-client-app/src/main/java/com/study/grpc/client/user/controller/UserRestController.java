package com.study.grpc.client.user.controller;

import com.study.grpc.client.user.UserGrpcClient;
import com.study.grpc.client.user.dto.UserCreateRequest;
import com.study.grpc.client.user.dto.UserResponse;
import com.study.grpc.client.user.dto.UserBatchCreateRequest;
import com.study.grpc.client.user.dto.UserBatchCreateResponse;
import com.study.grpc.client.user.dto.UserListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User REST API Controller (BFF Pattern)
 *
 * 브라우저 → REST API (HTTP/JSON) → gRPC Client → gRPC Server
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserRestController {

    private final UserGrpcClient userGrpcClient;

    /**
     * 사용자 생성 (Unary RPC)
     * POST /api/user
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest request) {
        log.info("REST API: 사용자 생성 요청 - email={}", request.getEmail());

        try {
            com.study.grpc.proto.CreateUserResponse grpcResponse = userGrpcClient.createUser(
                request.getEmail(),
                request.getName(),
                request.getPhoneNumber() != null ? request.getPhoneNumber() : ""
            );

            UserResponse response = UserResponse.builder()
                .id(grpcResponse.getId())
                .email(grpcResponse.getEmail())
                .name(grpcResponse.getName())
                .phoneNumber(grpcResponse.getPhoneNumber())
                .status(grpcResponse.getStatus())
                .createdAt(grpcResponse.getCreatedAt())
                .updatedAt(grpcResponse.getUpdatedAt())
                .build();

            log.info("REST API: 사용자 생성 성공 - id={}", response.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("REST API: 사용자 생성 실패", e);
            throw new RuntimeException("사용자 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 조회 (Unary RPC)
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        log.info("REST API: 사용자 조회 요청 - id={}", id);

        try {
            com.study.grpc.proto.GetUserResponse grpcResponse = userGrpcClient.getUser(id);

            UserResponse response = UserResponse.builder()
                .id(grpcResponse.getId())
                .email(grpcResponse.getEmail())
                .name(grpcResponse.getName())
                .phoneNumber(grpcResponse.getPhoneNumber())
                .status(grpcResponse.getStatus())
                .createdAt(grpcResponse.getCreatedAt())
                .updatedAt(grpcResponse.getUpdatedAt())
                .build();

            log.info("REST API: 사용자 조회 성공 - id={}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("REST API: 사용자 조회 실패 - id={}", id, e);
            throw new RuntimeException("사용자 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 목록 조회 (Server Streaming RPC)
     * GET /api/user?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<UserListResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST API: 사용자 목록 조회 요청 - page={}, size={}", page, size);

        try {
            // 서버 스트리밍은 비동기이므로, 여기서는 간단히 메시지만 반환
            // 실제로는 WebFlux나 SSE를 사용할 수 있습니다
            userGrpcClient.getUsers(page, size);

            // 테스트용 응답
            UserListResponse response =
                UserListResponse.builder()
                .users(new ArrayList<>())
                .totalCount(0)
                .build();

            log.info("REST API: 사용자 목록 조회 - Server Streaming 시작");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("REST API: 사용자 목록 조회 실패", e);
            throw new RuntimeException("사용자 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 일괄 생성 (Client Streaming RPC)
     * POST /api/user/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<UserBatchCreateResponse> batchCreateUsers(
            @RequestBody UserBatchCreateRequest request) {
        log.info("REST API: 사용자 일괄 생성 요청 - count={}", request.getUsers().size());

        try {
            List<com.study.grpc.proto.CreateUserRequest> grpcRequests = request.getUsers().stream()
                .map(user -> com.study.grpc.proto.CreateUserRequest.newBuilder()
                    .setEmail(user.getEmail())
                    .setName(user.getName())
                    .setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")
                    .build())
                .collect(Collectors.toList());

            com.study.grpc.proto.BatchCreateUsersResponse grpcResponse = userGrpcClient.batchCreateUsers(grpcRequests);

            UserBatchCreateResponse response =
                UserBatchCreateResponse.builder()
                .createdCount(grpcResponse.getCreatedCount())
                .userIds(grpcResponse.getUserIdsList())
                .build();

            log.info("REST API: 사용자 일괄 생성 성공 - count={}", response.getCreatedCount());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("REST API: 사용자 일괄 생성 실패", e);
            throw new RuntimeException("사용자 일괄 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }
}

