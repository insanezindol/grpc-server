package com.example.grpcserver.config;

import com.example.grpcserver.grpc.GrpcMemberServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrpcServerConfig {

    @Value("${grpc.server.port}")
    private int grpcPort;

    private final GrpcMemberServiceImpl grpcMemberService;

    @Bean
    public Server grpcServer() throws IOException {
        Server server = ServerBuilder.forPort(grpcPort)
                .addService(grpcMemberService)
                .addService(ProtoReflectionService.newInstance()) // gRPC reflection for testing
                .build();

        server.start();
        log.info("gRPC Server started on port: {}", grpcPort);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server");
            server.shutdown();
        }));

        return server;
    }
}
