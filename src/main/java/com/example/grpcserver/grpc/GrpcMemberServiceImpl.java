package com.example.grpcserver.grpc;

import com.example.grpcserver.entity.Member;
import com.example.grpcserver.repository.MemberRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcMemberServiceImpl extends MemberServiceGrpc.MemberServiceImplBase {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void createMember(MemberRequest request, StreamObserver<MemberResponse> responseObserver) {
        try {
            Member member = Member.builder()
                    .name(request.getName())
                    .age(request.getAge())
                    .build();

            Member savedMember = memberRepository.save(member);

            MemberResponse response = MemberResponse.newBuilder()
                    .setId(savedMember.getId())
                    .setName(savedMember.getName())
                    .setAge(savedMember.getAge())
                    .setMessage("Member created successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC: Member created - ID: {}", savedMember.getId());
        } catch (Exception e) {
            log.error("gRPC: Failed to create member", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getMember(GetMemberRequest request, StreamObserver<MemberResponse> responseObserver) {
        try {
            memberRepository.findById(request.getId())
                    .ifPresentOrElse(
                            member -> {
                                MemberResponse response = MemberResponse.newBuilder()
                                        .setId(member.getId())
                                        .setName(member.getName())
                                        .setAge(member.getAge())
                                        .setMessage("Member found")
                                        .build();
                                responseObserver.onNext(response);
                                responseObserver.onCompleted();
                                log.info("gRPC: Member found - ID: {}", member.getId());
                            },
                            () -> {
                                responseObserver.onError(
                                        new RuntimeException("Member not found with id: " + request.getId())
                                );
                                log.warn("gRPC: Member not found - ID: {}", request.getId());
                            }
                    );
        } catch (Exception e) {
            log.error("gRPC: Failed to get member", e);
            responseObserver.onError(e);
        }
    }

    @Override
    @Transactional
    public void updateMember(MemberRequest request, StreamObserver<MemberResponse> responseObserver) {
        try {
            memberRepository.findById(request.getId())
                    .ifPresentOrElse(
                            member -> {
                                member.setName(request.getName());
                                member.setAge(request.getAge());
                                Member updatedMember = memberRepository.save(member);

                                MemberResponse response = MemberResponse.newBuilder()
                                        .setId(updatedMember.getId())
                                        .setName(updatedMember.getName())
                                        .setAge(updatedMember.getAge())
                                        .setMessage("Member updated successfully")
                                        .build();

                                responseObserver.onNext(response);
                                responseObserver.onCompleted();
                                log.info("gRPC: Member updated - ID: {}", updatedMember.getId());
                            },
                            () -> {
                                responseObserver.onError(
                                        new RuntimeException("Member not found with id: " + request.getId())
                                );
                                log.warn("gRPC: Member not found for update - ID: {}", request.getId());
                            }
                    );
        } catch (Exception e) {
            log.error("gRPC: Failed to update member", e);
            responseObserver.onError(e);
        }
    }

    @Override
    @Transactional
    public void deleteMember(DeleteMemberRequest request, StreamObserver<DeleteMemberResponse> responseObserver) {
        try {
            boolean exists = memberRepository.existsById(request.getId());

            if (exists) {
                memberRepository.deleteById(request.getId());
                DeleteMemberResponse response = DeleteMemberResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Member deleted successfully")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                log.info("gRPC: Member deleted - ID: {}", request.getId());
            } else {
                DeleteMemberResponse response = DeleteMemberResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Member not found with id: " + request.getId())
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                log.warn("gRPC: Member not found for delete - ID: {}", request.getId());
            }
        } catch (Exception e) {
            log.error("gRPC: Failed to delete member", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void listMembers(ListMembersRequest request, StreamObserver<ListMembersResponse> responseObserver) {
        try {
            List<Member> members = memberRepository.findAll();

            List<MemberResponse> memberResponses = members.stream()
                    .map(member -> MemberResponse.newBuilder()
                            .setId(member.getId())
                            .setName(member.getName())
                            .setAge(member.getAge())
                            .setMessage("")
                            .build())
                    .collect(Collectors.toList());

            ListMembersResponse response = ListMembersResponse.newBuilder()
                    .addAllMembers(memberResponses)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC: Listed {} members", members.size());
        } catch (Exception e) {
            log.error("gRPC: Failed to list members", e);
            responseObserver.onError(e);
        }
    }
}
