package com.likelion.basecode.tag.application;

import com.likelion.basecode.tag.api.dto.request.TagSaveRequestDto;
import com.likelion.basecode.tag.api.dto.request.TagUpdateRequestDto;
import com.likelion.basecode.tag.api.dto.response.TagInfoResponseDto;
import com.likelion.basecode.tag.api.dto.response.TagListResponseDto;
import com.likelion.basecode.tag.domain.Tag;
import com.likelion.basecode.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    // 태그 저장
    @Transactional
    public void createTag(TagSaveRequestDto tagSaveRequestDto){
        Tag tag = Tag.builder()
                .name(tagSaveRequestDto.name())
                .build();
        tagRepository.save(tag);
    }

    // 태그 전체 조회
    public TagListResponseDto getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return TagListResponseDto.from(
                tags.stream()
                        .map(TagInfoResponseDto::from)
                        .collect(Collectors.toList())
        );
    }

    // 태그 단건 조회
    public TagInfoResponseDto getTagById(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("해당 태그가 없습니다. id=" + tagId));
        return TagInfoResponseDto.from(tag);
    }

    // 태그 수정
    @Transactional
    public void tagUpdate(Long tagId, TagUpdateRequestDto tagUpdateRequestDto) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("해당 태그가 없습니다. id=" + tagId));
        tag.update(tagUpdateRequestDto);
    }

    // 태그 삭제
    @Transactional
    public void tagDelete(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("해당 태그가 없습니다. id=" + tagId));
        tag.getPostTags().clear();
        tagRepository.delete(tag);
    }
}
