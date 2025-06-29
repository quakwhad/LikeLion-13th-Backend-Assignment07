package com.likelion.basecode.movie.api.dto.response;

public record MovieResponseDto(
        String movieCd,
        String movieNm,	    // 문자열 영화명을 출력합니다.
        String openDt,      // 문자열 개봉일을 출력합니다.
        String repNationNm, // 문자열 대표 제작국가명을 출력합니다.
        String GenreNm	    // 문자열 전체 장르명을 출력합니다.
) {}
