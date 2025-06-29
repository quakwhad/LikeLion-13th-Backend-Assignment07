package com.likelion.basecode.posttag.domain.repository;

import com.likelion.basecode.post.domain.Post;
import com.likelion.basecode.posttag.domain.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PostTag pt WHERE pt.post = :post")
    void deleteAllByPost(@Param("post") Post post);
}
