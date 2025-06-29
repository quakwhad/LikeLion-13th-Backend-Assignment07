package com.likelion.basecode.movie.api;

import com.likelion.basecode.movie.api.dto.response.MovieListResponseDto;
import com.likelion.basecode.movie.application.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies") // 기본 URL 경로 설정
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // 일별 박스오피스 전체 조회
    @GetMapping("/daily-boxoffice")
    public ResponseEntity<MovieListResponseDto> getDailyBoxOffice() {
        MovieListResponseDto response = movieService.fetchAllDailyBoxOfficeMoviesWithGenres();
        return ResponseEntity.ok(response);
    }

    // 게시글 ID 기반 영화 추천
    @GetMapping("/recommend/{postId}")
    public ResponseEntity<MovieListResponseDto> recommendMovies(@PathVariable Long postId) {
        MovieListResponseDto response = movieService.recommendMoviesByPostId(postId);
        return ResponseEntity.ok(response);
    }
}