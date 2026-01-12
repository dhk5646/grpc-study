package com.study.grpc.client;

import com.study.grpc.proto.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * gRPC 클라이언트 애플리케이션
 *
 * gRPC 서버와 통신하는 클라이언트 애플리케이션
 * CommandLineRunner를 통해 자동으로 gRPC 호출 테스트 실행
 */
@SpringBootApplication
public class GrpcClientApplication {

    static void main(String[] args) {
        SpringApplication.run(GrpcClientApplication.class, args);
    }
}

