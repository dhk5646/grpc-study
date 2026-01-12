package com.study.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * gRPC 서버 애플리케이션
 *
 * User 도메인 및 gRPC 서비스를 제공하는 서버
 */
@SpringBootApplication(scanBasePackages = "com.study.grpc")
public class GrpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcServerApplication.class, args);
    }
}

