package com.likelion.basecode.movie.api.dto.response;

public record MovieResponseDto(
        String movieCd,
        String movieNm,	    // 영화명을 출력합니다.
        String openDt,      // 개봉일을 출력합니다.
        String repNationNm, // 대표 제작국가명을 출력합니다.
        String GenreNm	    // 전체 장르명을 출력합니다.
) {}
