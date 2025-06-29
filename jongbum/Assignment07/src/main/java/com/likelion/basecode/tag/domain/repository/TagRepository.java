package com.likelion.basecode.tag.domain.repository;

import com.likelion.basecode.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);
}
