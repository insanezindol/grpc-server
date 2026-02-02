package com.example.grpcserver.service;

import com.example.grpcserver.dto.MemberRequest;
import com.example.grpcserver.dto.MemberResponse;
import com.example.grpcserver.entity.Member;
import com.example.grpcserver.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse createMember(MemberRequest request) {
        log.info("Service: Creating member - Name: {}", request.getName());

        Member member = Member.builder()
                .name(request.getName())
                .age(request.getAge())
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("Service: Member created - ID: {}", savedMember.getId());

        return toResponse(savedMember);
    }

    public MemberResponse getMember(Long id) {
        log.info("Service: Getting member - ID: {}", id);

        return memberRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> {
                    log.warn("Service: Member not found - ID: {}", id);
                    return new RuntimeException("Member not found with id: " + id);
                });
    }

    public List<MemberResponse> getAllMembers() {
        log.info("Service: Getting all members");

        List<Member> members = memberRepository.findAll();
        log.info("Service: Found {} members", members.size());

        return members.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberResponse updateMember(Long id, MemberRequest request) {
        log.info("Service: Updating member - ID: {}", id);

        return memberRepository.findById(id)
                .map(existingMember -> {
                    existingMember.setName(request.getName());
                    existingMember.setAge(request.getAge());

                    Member updatedMember = memberRepository.save(existingMember);
                    log.info("Service: Member updated - ID: {}", id);

                    return toResponse(updatedMember);
                })
                .orElseThrow(() -> {
                    log.warn("Service: Member not found for update - ID: {}", id);
                    return new RuntimeException("Member not found with id: " + id);
                });
    }

    @Transactional
    public void deleteMember(Long id) {
        log.info("Service: Deleting member - ID: {}", id);

        if (!memberRepository.existsById(id)) {
            log.warn("Service: Member not found for delete - ID: {}", id);
            throw new RuntimeException("Member not found with id: " + id);
        }

        memberRepository.deleteById(id);
        log.info("Service: Member deleted - ID: {}", id);
    }

    private MemberResponse toResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getAge()
        );
    }

}
