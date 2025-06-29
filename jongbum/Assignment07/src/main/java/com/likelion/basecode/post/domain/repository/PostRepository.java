package com.likelion.basecode.post.domain.repository;

import com.likelion.basecode.member.domain.Member;
import com.likelion.basecode.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByMember(Member member);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.member " +
            "LEFT JOIN FETCH p.postTags pt " +
            "LEFT JOIN FETCH pt.tag " +
            "WHERE p.postId = :postId")
    Optional<Post> findByIdWithTags(@Param("postId") Long postId);
}
