package com.likelion.basecode.member.api;

import com.likelion.basecode.common.error.SuccessCode;
import com.likelion.basecode.common.template.ApiResTemplate;
import com.likelion.basecode.member.api.dto.response.MemberInfoResponseDto;
import com.likelion.basecode.member.api.dto.response.MemberListResponseDto;
import com.likelion.basecode.member.api.dto.request.MemberSaveRequestDto;
import com.likelion.basecode.member.api.dto.request.MemberUpdateRequestDto;
import com.likelion.basecode.member.application.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    // 사용자 저장
    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResTemplate<String> memberSave(@RequestBody MemberSaveRequestDto memberSaveRequestDto) {
        memberService.memberSave(memberSaveRequestDto);
        return ApiResTemplate.successWithNoContent(SuccessCode.MEMBER_SAVE_SUCCESS);
    }

    // 사용자 전체 조회
    @GetMapping("/all")
    public ApiResTemplate<MemberListResponseDto> memberFindAll() {
        MemberListResponseDto memberListResponseDto = memberService.memberFindAll();
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, memberListResponseDto);
    }

    // 회원 id를 통해 특정 사용자 조회
    @GetMapping("/{memberId}")
    public ApiResTemplate<MemberInfoResponseDto> memberFindOne(@PathVariable("memberId") Long memberId) {
        MemberInfoResponseDto memberInfoResponseDto = memberService.memberFindOne(memberId);
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, memberInfoResponseDto);
    }

    // 회원 id를 통한 사용자 수정
    @PatchMapping("/{memberId}")
    public ApiResTemplate<String> memberUpdate(@PathVariable("memberId") Long memberId,
                                               @RequestBody MemberUpdateRequestDto memberUpdateRequestDto) {
        memberService.memberUpdate(memberId, memberUpdateRequestDto);
        return ApiResTemplate.successWithNoContent(SuccessCode.MEMBER_UPDATE_SUCCESS);
    }

    // 회원 id를 통한 사용자 삭제
    @DeleteMapping("/{memberId}")
    public ApiResTemplate<String> memberDelete(@PathVariable("memberId") Long memberId) {
        memberService.memberDelete(memberId);
        return ApiResTemplate.successWithNoContent(SuccessCode.MEMBER_DELETE_SUCCESS);
    }
}
