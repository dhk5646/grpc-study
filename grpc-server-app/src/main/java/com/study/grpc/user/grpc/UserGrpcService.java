package com.study.grpc.user.grpc;

import com.study.grpc.proto.*;
import com.study.grpc.user.service.UserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * User gRPC Service
 *
 * gRPC 요청을 처리하고 비즈니스 로직(UserService)을 호출
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        log.info("gRPC CreateUser request: email={}", request.getEmail());

        try {
            // DTO 변환
            com.study.grpc.user.dto.UserCreateRequest createRequest =
                com.study.grpc.user.dto.UserCreateRequest.builder()
                    .email(request.getEmail())
                    .password("TempPassword123!")  // 임시 비밀번호
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .build();

            // 비즈니스 로직 호출
            com.study.grpc.user.dto.UserResponse userResponse = userService.createUser(createRequest);

            // gRPC 응답 변환
            CreateUserResponse response = CreateUserResponse.newBuilder()
                .setId(userResponse.getId())
                .setEmail(userResponse.getEmail())
                .setName(userResponse.getName())
                .setPhoneNumber(userResponse.getPhoneNumber() != null ? userResponse.getPhoneNumber() : "")
                .setStatus(userResponse.getStatus().name())
                .setCreatedAt(userResponse.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC))
                .setUpdatedAt(userResponse.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC CreateUser success: id={}", userResponse.getId());

        } catch (Exception e) {
            log.error("gRPC CreateUser error", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        log.info("gRPC GetUser request: id={}", request.getId());

        try {
            com.study.grpc.user.dto.UserResponse userResponse =
                userService.getUserById(request.getId());

            GetUserResponse response = GetUserResponse.newBuilder()
                .setId(userResponse.getId())
                .setEmail(userResponse.getEmail())
                .setName(userResponse.getName())
                .setPhoneNumber(userResponse.getPhoneNumber() != null ? userResponse.getPhoneNumber() : "")
                .setStatus(userResponse.getStatus().name())
                .setCreatedAt(userResponse.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC))
                .setUpdatedAt(userResponse.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC GetUser success: id={}", request.getId());

        } catch (Exception e) {
            log.error("gRPC GetUser error", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getUsers(GetUsersRequest request, StreamObserver<GetUsersResponse> responseObserver) {
        log.info("gRPC GetUsers request: page={}, size={}", request.getPage(), request.getSize());

        try {
            List<com.study.grpc.user.dto.UserResponse> users = userService.getAllUsers();

            // 페이징 처리
            int page = request.getPage();
            int size = request.getSize() > 0 ? request.getSize() : 10;
            int start = page * size;
            int end = Math.min(start + size, users.size());

            if (start < users.size()) {
                for (int i = start; i < end; i++) {
                    com.study.grpc.user.dto.UserResponse user = users.get(i);

                    GetUsersResponse response = GetUsersResponse.newBuilder()
                        .setId(user.getId())
                        .setEmail(user.getEmail())
                        .setName(user.getName())
                        .setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")
                        .setStatus(user.getStatus().name())
                        .setCreatedAt(user.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC))
                        .setUpdatedAt(user.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC))
                        .build();

                    responseObserver.onNext(response);
                    log.info("Sent user: id={}, email={}", user.getId(), user.getEmail());

                    try {
                        Thread.sleep(100); // 스트리밍 효과
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            responseObserver.onCompleted();
            log.info("gRPC GetUsers completed");

        } catch (Exception e) {
            log.error("gRPC GetUsers error", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public StreamObserver<CreateUserRequest> batchCreateUsers(
            StreamObserver<BatchCreateUsersResponse> responseObserver) {

        return new StreamObserver<>() {
            private final List<Long> createdIds = new ArrayList<>();
            private int count = 0;

            @Override
            public void onNext(CreateUserRequest request) {
                log.info("Received batch create request: email={}", request.getEmail());

                try {
                    com.study.grpc.user.dto.UserCreateRequest createRequest =
                        com.study.grpc.user.dto.UserCreateRequest.builder()
                            .email(request.getEmail())
                            .password("TempPassword123!")
                            .name(request.getName())
                            .phoneNumber(request.getPhoneNumber())
                            .build();

                    com.study.grpc.user.dto.UserResponse userResponse =
                        userService.createUser(createRequest);

                    createdIds.add(userResponse.getId());
                    count++;

                } catch (Exception e) {
                    log.error("Error creating user in batch", e);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error in batch create users", t);
            }

            @Override
            public void onCompleted() {
                BatchCreateUsersResponse response = BatchCreateUsersResponse.newBuilder()
                    .setCreatedCount(count)
                    .addAllUserIds(createdIds)
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();

                log.info("Batch create users completed: count={}", count);
            }
        };
    }

    @Override
    public StreamObserver<SyncUsersRequest> syncUsers(
            StreamObserver<SyncUsersResponse> responseObserver) {

        return new StreamObserver<>() {
            @Override
            public void onNext(SyncUsersRequest request) {
                log.info("Received sync request: action={}, userId={}", request.getAction(), request.getUserId());

                try {
                    String status;
                    String message;

                    switch (request.getAction().toUpperCase()) {
                        case "CREATE":
                            // 사용자 생성
                            com.study.grpc.user.dto.UserCreateRequest createRequest =
                                com.study.grpc.user.dto.UserCreateRequest.builder()
                                    .email(request.getEmail())
                                    .password("TempPassword123!")
                                    .name(request.getName())
                                    .phoneNumber(request.getPhoneNumber())
                                    .build();

                            com.study.grpc.user.dto.UserResponse createdUser = userService.createUser(createRequest);
                            status = "SUCCESS";
                            message = "User created: id=" + createdUser.getId();
                            break;

                        case "UPDATE":
                            // 사용자 수정
                            com.study.grpc.user.dto.UserUpdateRequest updateRequest =
                                com.study.grpc.user.dto.UserUpdateRequest.builder()
                                    .name(request.getName())
                                    .phoneNumber(request.getPhoneNumber())
                                    .build();

                            com.study.grpc.user.dto.UserResponse updatedUser =
                                userService.updateUser(request.getUserId(), updateRequest);
                            status = "SUCCESS";
                            message = "User updated: id=" + updatedUser.getId();
                            break;

                        case "DELETE":
                            // 사용자 삭제
                            userService.deleteUser(request.getUserId());
                            status = "SUCCESS";
                            message = "User deleted: id=" + request.getUserId();
                            break;

                        default:
                            status = "ERROR";
                            message = "Unknown action: " + request.getAction();
                    }

                    SyncUsersResponse response = SyncUsersResponse.newBuilder()
                        .setStatus(status)
                        .setMessage(message)
                        .setTimestamp(Instant.now().toEpochMilli())
                        .build();

                    responseObserver.onNext(response);
                    log.info("Sent sync response: {}", message);

                } catch (Exception e) {
                    log.error("Error processing sync", e);

                    SyncUsersResponse errorResponse = SyncUsersResponse.newBuilder()
                        .setStatus("ERROR")
                        .setMessage(e.getMessage())
                        .setTimestamp(Instant.now().toEpochMilli())
                        .build();

                    responseObserver.onNext(errorResponse);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error in sync users", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                log.info("Sync users completed");
            }
        };
    }
}

