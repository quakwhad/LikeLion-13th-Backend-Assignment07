package com.likelion.basecode.post.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record  PostSaveRequestDto(
        @NotNull(message = "작성자를 필수로 입력해야 합니다.")
        Long memberId,
        @NotBlank(message = "제목을 필수로 입력해야 합니다.")
        @Size(min = 3, max = 20)
        String title,
        @NotBlank(message = "내용을 필수로 입력해야 합니다.")
        @Size(min = 3, max = 100)
        String contents
) {
}
