package com.likelion.basecode.member.application;

import com.likelion.basecode.common.error.ErrorCode;
import com.likelion.basecode.common.exception.BusinessException;
import com.likelion.basecode.member.api.dto.response.MemberInfoResponseDto;
import com.likelion.basecode.member.api.dto.response.MemberListResponseDto;
import com.likelion.basecode.member.api.dto.request.MemberSaveRequestDto;
import com.likelion.basecode.member.api.dto.request.MemberUpdateRequestDto;
import com.likelion.basecode.member.domain.Member;
import com.likelion.basecode.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    // 사용자 정보 저장
    @Transactional
    public void memberSave(MemberSaveRequestDto memberSaveRequestDto) {
        Member member = Member.builder()
                .name(memberSaveRequestDto.name())
                .age(memberSaveRequestDto.age())
                .part(memberSaveRequestDto.part())
                .build();
        memberRepository.save(member);
    }

    // 사용자 모두 조회
    public MemberListResponseDto memberFindAll() {
        List<Member> members = memberRepository.findAll();
        List<MemberInfoResponseDto> memberInfoResponseDtoList = members.stream()
                .map(MemberInfoResponseDto::from)
                .toList();
        return MemberListResponseDto.from(memberInfoResponseDtoList);
    }

    // 단일 사용자 조회
    public MemberInfoResponseDto memberFindOne(Long memberId) {
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND_EXCEPTION,
                                ErrorCode.MEMBER_NOT_FOUND_EXCEPTION.getMessage() + memberId)
                );
        return MemberInfoResponseDto.from(member);
    }

    // 사용자 정보 수정
    @Transactional
    public void memberUpdate(Long memberId,
                             MemberUpdateRequestDto memberUpdateRequestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND_EXCEPTION,
                                ErrorCode.MEMBER_NOT_FOUND_EXCEPTION.getMessage() + memberId)
                );

        member.update(memberUpdateRequestDto);
    }

    // 사용자 정보 삭제
    @Transactional
    public void memberDelete(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND_EXCEPTION,
                                ErrorCode.MEMBER_NOT_FOUND_EXCEPTION.getMessage() + memberId)
                );

        memberRepository.delete(member);
    }
}
