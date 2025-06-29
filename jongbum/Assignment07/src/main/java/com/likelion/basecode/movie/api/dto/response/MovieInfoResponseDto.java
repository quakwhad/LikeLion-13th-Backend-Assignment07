package com.likelion.basecode.movie.api.dto.response;

// 이 DTO는 KOBIS 영화 상세 정보 API (searchMovieInfo)로부터
// 가져올 영화의 세부 정보, 특히 장르 정보를 담습니다.
public record MovieInfoResponseDto(
        String genre // 콤마로 구분된 전체 장르명 (예: "드라마, 액션")
) {}