package com.likelion.basecode.post.application;

import com.likelion.basecode.common.client.TagRecommendationClient;
import com.likelion.basecode.common.error.ErrorCode;
import com.likelion.basecode.common.exception.BusinessException;
import com.likelion.basecode.common.s3.S3Uploader;
import com.likelion.basecode.member.domain.Member;
import com.likelion.basecode.member.domain.repository.MemberRepository;
import com.likelion.basecode.post.api.dto.response.PostInfoResponseDto;
import com.likelion.basecode.post.api.dto.response.PostListResponseDto;
import com.likelion.basecode.post.api.dto.request.PostSaveRequestDto;
import com.likelion.basecode.post.api.dto.request.PostUpdateRequestDto;
import com.likelion.basecode.post.domain.Post;
import com.likelion.basecode.post.domain.repository.PostRepository;
import com.likelion.basecode.posttag.domain.PostTag;
import com.likelion.basecode.posttag.domain.repository.PostTagRepository;
import com.likelion.basecode.tag.domain.Tag;
import com.likelion.basecode.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;



@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final TagRecommendationClient tagClient;
    private final S3Uploader s3Uploader;

    // 게시물 저장
    @Transactional
    public PostInfoResponseDto postSave(PostSaveRequestDto postSaveRequestDto, MultipartFile imageFile) {
        Member member = memberRepository.findById(postSaveRequestDto.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND_EXCEPTION,
                        ErrorCode.MEMBER_NOT_FOUND_EXCEPTION.getMessage() + postSaveRequestDto.memberId()));

        String imageUrl = null;
        if(imageFile != null && !imageFile.isEmpty()) {
            imageUrl = s3Uploader.upload(imageFile, "post-images");
        }

        Post post = Post.builder()
                .title(postSaveRequestDto.title())
                .contents(postSaveRequestDto.contents())
                .imageUrl(imageUrl)
                .member(member)
                .build();

        postRepository.save(post);

        // AI 기반 추천 태그 추출 및 등록
        List<String> tagNames = tagClient.getRecommendedTags(post.getContents());
        registerTagsToPost(post, tagNames);

        // Fetch Join으로 태그 포함된 post 다시 조회
        // postSave() 이후 바로 반환하면, post.getPostTags()는 LAZY 로딩이기 때문에
        // tag 정보가 초기화되지 않아 tags가 누락된 채 응답될 수 있기 때문
        Post postWithTags = postRepository.findByIdWithTags(post.getPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND_EXCEPTION,
                        ErrorCode.POST_NOT_FOUND_EXCEPTION.getMessage() + post.getPostId()));
        
        return PostInfoResponseDto.from(postWithTags);
    }



    // 특정 작성자가 작성한 게시글 목록을 조회
    public PostListResponseDto postFindMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND_EXCEPTION,
                        ErrorCode.MEMBER_NOT_FOUND_EXCEPTION.getMessage() + memberId));

        List<Post> posts = postRepository.findByMember(member);
        List<PostInfoResponseDto> postInfoResponseDtos = posts.stream()
                .map(PostInfoResponseDto::from)
                .toList();

        return PostListResponseDto.from(postInfoResponseDtos);
    }

    // 게시물 수정
    @Transactional
    public PostInfoResponseDto postUpdate(Long postId,
                                          PostUpdateRequestDto postUpdateRequestDto, MultipartFile imageFile) {
        // 기존 게시물 조회 (이미지 URL과 태그 정보를 가져오기 위함)
        // findByIdWithTags를 사용하여 PostTag 컬렉션이 즉시 로딩되도록 보장합니다.
        Post post = postRepository.findByIdWithTags(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND_EXCEPTION,
                        ErrorCode.POST_NOT_FOUND_EXCEPTION.getMessage() + postId));

        // 기존 이미지 URL을 미리 저장합니다. (새 이미지 업로드 후 삭제하기 위함)
        String oldImageUrl = post.getImageUrl();

        String newImageUrl = null;

        // 새 이미지 파일이 제공된 경우
        if(imageFile != null && !imageFile.isEmpty()) {
            // 새 이미지 S3에 업로드
            newImageUrl = s3Uploader.upload(imageFile, "post-images");

            // 이전 이미지가 존재하고, 새 이미지가 성공적으로 업로드된 경우에만 이전 이미지 삭제
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                s3Uploader.deleteFile(oldImageUrl); // <-- 이전 이미지 삭제 함수 호출!
            }
            // 게시물 엔티티의 이미지 URL을 새 이미지 URL로 업데이트
            post.updateImage(newImageUrl);
        }

        // 게시물 내용 (제목, 본문) 업데이트
        post.update(postUpdateRequestDto);

        // 태그 관련 로직
        // 기존 PostTag 연관 관계 삭제 (DB에서 직접 삭제)
        postTagRepository.deleteAllByPost(post);
        // 영속성 컨텍스트에 있는 Post 엔티티의 postTags 컬렉션도 비웁니다.
        // post는 findByIdWithTags로 로드되었으므로, 이 시점에서 컬렉션은 초기화되어 있습니다.
        post.getPostTags().clear(); // 양방향 매핑에서 컬렉션도 비워줘야 합니다.

        // AI 기반 추천 태그 추출 및 재등록
        List<String> tagNames = tagClient.getRecommendedTags(post.getContents());
        registerTagsToPost(post, tagNames);

        return PostInfoResponseDto.from(post);
    }

    // 게시물 삭제
    @Transactional
    public void postDelete(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND_EXCEPTION,
                        ErrorCode.POST_NOT_FOUND_EXCEPTION.getMessage() + postId));

        // 게시물 삭제 전에 S3 이미지도 삭제
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            s3Uploader.deleteFile(post.getImageUrl());
        }

        postRepository.delete(post);
    }

    // 게시물에서 이미지만 삭제
    @Transactional
    public PostInfoResponseDto removePostImage(Long postId,
                                PostUpdateRequestDto postUpdateRequestDto, MultipartFile imageFile) {
        // 기존 게시물 조회 (이미지 URL과 태그 정보를 가져오기 위함)
        // findByIdWithTags를 사용하여 PostTag 컬렉션이 즉시 로딩되도록 보장합니다.
        Post post = postRepository.findByIdWithTags(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND_EXCEPTION,
                        ErrorCode.POST_NOT_FOUND_EXCEPTION.getMessage() + postId));

        // 기존 이미지 URL을 미리 저장합니다. (새 이미지 업로드 후 삭제하기 위함)
        String oldImageUrl = post.getImageUrl();

        s3Uploader.deleteFile(oldImageUrl);
        post.updateImage(null); // DB에서도 이미지 URL을 null로 설정

        // 게시물 내용 (제목, 본문) 업데이트
        post.update(postUpdateRequestDto);

        // 태그 관련 로직
        // 기존 PostTag 연관 관계 삭제 (DB에서 직접 삭제)
        postTagRepository.deleteAllByPost(post);
        post.getPostTags().clear(); // 양방향 매핑에서 컬렉션도 비워줘야 합니다.

        // AI 기반 추천 태그 추출 및 재등록
        List<String> tagNames = tagClient.getRecommendedTags(post.getContents());
        registerTagsToPost(post, tagNames);

        // 'post' 객체는 이미 findByIdWithTags로 로드되어 트랜잭션 내에서 관리되고 있으며,
        // 모든 변경사항이 반영되어 태그 컬렉션도 업데이트된 상태입니다.
        // 따라서 별도로 다시 조회할 필요 없이 이 객체를 바로 DTO로 변환하여 반환합니다.
        return PostInfoResponseDto.from(post);
    }

    // 게시물 추천 태그 목록 등록 및 PostTag 연관 엔티티 저장
    private void registerTagsToPost(Post post, List<String> tagNames) {
        for (String tagName : tagNames) {
            // 기존 태그가 있다면 사용, 없으면 새로 생성
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(new Tag(tagName)));

            // PostTag 생성 및 연관 관계 추가
            PostTag postTag = new PostTag(post, tag);
            post.getPostTags().add(postTag);    // 양방향 매핑 유지
            postTagRepository.save(postTag);
        }
    }
}
