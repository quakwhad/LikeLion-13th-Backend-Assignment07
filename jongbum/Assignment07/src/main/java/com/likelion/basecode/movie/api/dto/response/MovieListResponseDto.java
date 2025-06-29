package com.likelion.basecode.movie.api.dto.response;


import java.util.List;

public record MovieListResponseDto(
        List<MovieResponseDto> movies
) {}
