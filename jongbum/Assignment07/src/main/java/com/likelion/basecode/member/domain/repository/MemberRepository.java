package com.likelion.basecode.member.domain.repository;

import com.likelion.basecode.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
