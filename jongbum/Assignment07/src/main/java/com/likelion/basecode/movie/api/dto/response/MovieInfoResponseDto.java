package com.likelion.basecode.movie.api.dto.response;

// 이 DTO는 KOBIS 영화 상세 정보 API (searchMovieInfo)로부터 장르 정보 저장
public record MovieInfoResponseDto(
        String genre // 콤마로 구분된 전체 장르명 (예: "드라마, 액션")
) {}