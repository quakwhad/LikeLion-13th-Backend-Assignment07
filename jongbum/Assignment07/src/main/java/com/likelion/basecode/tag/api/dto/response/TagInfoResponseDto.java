package com.likelion.basecode.tag.api.dto.response;
import com.likelion.basecode.tag.domain.Tag;
import lombok.Builder;

@Builder
public record TagInfoResponseDto(
        Long id,
        String name
) {
    public static TagInfoResponseDto from(Tag tag) {
        return TagInfoResponseDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
