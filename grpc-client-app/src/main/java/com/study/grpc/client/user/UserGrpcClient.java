package com.study.grpc.client.user;

import com.study.grpc.proto.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * User gRPC 클라이언트
 *
 * gRPC를 통한 User 서비스 호출
 */
@Slf4j
@Service
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub blockingStub;

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceStub asyncStub;

    /**
     * 사용자 생성 (Unary RPC)
     */
    public CreateUserResponse createUser(String email, String name, String phoneNumber) {
        log.info("gRPC Client: Creating user - email={}", email);

        CreateUserRequest request = CreateUserRequest.newBuilder()
            .setEmail(email)
            .setName(name)
            .setPhoneNumber(phoneNumber)
            .build();

        CreateUserResponse response = blockingStub.createUser(request);
        log.info("gRPC Client: User created - id={}", response.getId());

        return response;
    }

    /**
     * 사용자 조회 (Unary RPC)
     */
    public GetUserResponse getUser(long userId) {
        log.info("gRPC Client: Getting user - id={}", userId);

        GetUserRequest request = GetUserRequest.newBuilder()
            .setId(userId)
            .build();

        GetUserResponse response = blockingStub.getUser(request);
        log.info("gRPC Client: User retrieved - email={}", response.getEmail());

        return response;
    }

    /**
     * 사용자 목록 조회 (Server Streaming RPC)
     */
    public void getUsers(int page, int size) {
        log.info("gRPC Client: Getting users - page={}, size={}", page, size);

        GetUsersRequest request = GetUsersRequest.newBuilder()
            .setPage(page)
            .setSize(size)
            .build();

        blockingStub.getUsers(request)
            .forEachRemaining(response ->
                log.info("gRPC Client: Received user - id={}, email={}",
                    response.getId(), response.getEmail())
            );

        log.info("gRPC Client: Get users completed");
    }

    /**
     * 사용자 일괄 생성 (Client Streaming RPC)
     */
    public BatchCreateUsersResponse batchCreateUsers(List<CreateUserRequest> requests)
            throws InterruptedException {
        log.info("gRPC Client: Batch creating {} users", requests.size());

        CountDownLatch latch = new CountDownLatch(1);
        final BatchCreateUsersResponse[] response = new BatchCreateUsersResponse[1];

        StreamObserver<CreateUserRequest> requestObserver = asyncStub.batchCreateUsers(
            new StreamObserver<>() {
                @Override
                public void onNext(BatchCreateUsersResponse value) {
                    response[0] = value;
                    log.info("gRPC Client: Batch create completed - count={}",
                        value.getCreatedCount());
                }

                @Override
                public void onError(Throwable t) {
                    log.error("gRPC Client: Error in batch create", t);
                    latch.countDown();
                }

                @Override
                public void onCompleted() {
                    log.info("gRPC Client: Batch create stream completed");
                    latch.countDown();
                }
            }
        );

        try {
            for (CreateUserRequest request : requests) {
                requestObserver.onNext(request);
                log.info("gRPC Client: Sent create request - email={}", request.getEmail());
                Thread.sleep(200);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();
        latch.await(10, TimeUnit.SECONDS);

        return response[0];
    }

    /**
     * 사용자 동기화 (Bidirectional Streaming RPC)
     */
    public void syncUsers(List<SyncUsersRequest> requests) throws InterruptedException {
        log.info("gRPC Client: Starting user sync with {} requests", requests.size());

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<SyncUsersRequest> requestObserver = asyncStub.syncUsers(
            new StreamObserver<>() {
                @Override
                public void onNext(SyncUsersResponse value) {
                    log.info("gRPC Client: Sync response - status={}, message={}",
                        value.getStatus(), value.getMessage());
                }

                @Override
                public void onError(Throwable t) {
                    log.error("gRPC Client: Error in sync", t);
                    latch.countDown();
                }

                @Override
                public void onCompleted() {
                    log.info("gRPC Client: Sync completed");
                    latch.countDown();
                }
            }
        );

        try {
            for (SyncUsersRequest request : requests) {
                requestObserver.onNext(request);
                log.info("gRPC Client: Sent sync request - action={}", request.getAction());
                Thread.sleep(500);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();
        latch.await(10, TimeUnit.SECONDS);
    }
}

