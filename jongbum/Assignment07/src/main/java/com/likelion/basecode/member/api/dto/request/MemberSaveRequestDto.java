package com.likelion.basecode.member.api.dto.request;

import com.likelion.basecode.member.domain.Part;

public record MemberSaveRequestDto(
        String name,
        int age,
        Part part
) {
}
