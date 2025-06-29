package com.likelion.basecode.post.domain;

import com.likelion.basecode.member.domain.Member;
import com.likelion.basecode.post.api.dto.request.PostUpdateRequestDto;
import com.likelion.basecode.posttag.domain.PostTag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @Column(name = "post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    @Builder
    private Post(String title, String contents, Member member, String imageUrl) {
        this.title = title;
        this.contents = contents;
        this.member = member;
        this.imageUrl = imageUrl;
    }

    public void update(PostUpdateRequestDto postUpdateRequestDto) {
        this.title = postUpdateRequestDto.title();
        this.contents = postUpdateRequestDto.contents();
    }

    public void updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
