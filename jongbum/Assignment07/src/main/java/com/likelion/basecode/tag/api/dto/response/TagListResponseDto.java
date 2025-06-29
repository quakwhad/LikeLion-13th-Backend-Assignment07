package com.likelion.basecode.tag.api.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record TagListResponseDto(
        List<TagInfoResponseDto> tags
) {
    public static TagListResponseDto from(List<TagInfoResponseDto> tags){
        return TagListResponseDto.builder()
                .tags(tags)
                .build();
    }
}
