package com.likelion.basecode.movie.application;

import com.likelion.basecode.common.client.MovieSearchClient;
import com.likelion.basecode.common.client.TagRecommendationClient;
import com.likelion.basecode.common.error.ErrorCode;
import com.likelion.basecode.common.exception.BusinessException;
import com.likelion.basecode.movie.api.dto.response.MovieInfoResponseDto;
import com.likelion.basecode.movie.api.dto.response.MovieListResponseDto;
import com.likelion.basecode.movie.api.dto.response.MovieResponseDto;
import com.likelion.basecode.post.domain.Post;
import com.likelion.basecode.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final PostRepository postRepository;
    private final TagRecommendationClient tagClient;
    private final MovieSearchClient movieSearchClient;

    // 일별 박스오피스 전체 영화 목록 조회 (장르 정보 포함)
    public MovieListResponseDto fetchAllDailyBoxOfficeMoviesWithGenres() { // 새로운 함수명
        String targetDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 1. 일별 박스오피스 기본 목록 조회 (movieCd 포함)
        List<MovieResponseDto> moviesWithoutGenres = movieSearchClient.fetchDailyBoxOffice(targetDate);

        // 2. 각 영화에 대해 상세 정보 API 호출하여 장르 정보 보강
        List<MovieResponseDto> moviesWithGenres = moviesWithoutGenres.stream()
                .map(movie -> {
                    // movieCd가 없으면 장르를 가져올 수 없음 (이런 경우 발생하면 에러 처리 또는 스킵)
                    if (movie.movieCd() == null || movie.movieCd().isEmpty()) {
                        System.err.println("MovieCd not found for movie: " + movie.movieNm());
                        return movie; // movieCd가 없으면 장르 없이 기존 DTO 반환
                    }
                    try {
                        MovieInfoResponseDto movieDetail = movieSearchClient.fetchMovieDetail(movie.movieCd());
                        // 새로운 MovieResponseDto 생성 (record는 불변이므로)
                        return new MovieResponseDto(
                                movie.movieCd(),
                                movie.movieNm(),
                                movie.openDt(),
                                movie.repNationNm(),
                                movieDetail.genre() // MovieInfoResponseDto에서 가져온 장르로 채움
                        );
                    } catch (BusinessException e) {
                        System.err.println("Failed to fetch movie detail for movieCd: " + movie.movieCd() + " - " + e.getMessage());
                        return movie; // 상세 정보 조회 실패 시, 장르 없이 기존 DTO 반환
                    }
                })
                .collect(Collectors.toList());

        return new MovieListResponseDto(moviesWithGenres);
    }

    // 기존 함수: 특정 게시글의 추천 태그를 기반으로 영화 추천 (이 함수도 변경된 DTO 사용)
    public MovieListResponseDto recommendMoviesByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND_EXCEPTION,
                        ErrorCode.POST_NOT_FOUND_EXCEPTION.getMessage()));

        List<String> tags = tagClient.getRecommendedTags(post.getContents());

        if (tags.isEmpty()) {
            throw new BusinessException(ErrorCode.TAG_RECOMMENDATION_EMPTY,
                    ErrorCode.TAG_RECOMMENDATION_EMPTY.getMessage());
        }

        // 여기서도 장르를 가져오려면 위 fetchAllDailyBoxOfficeMoviesWithGenres()를 호출해야 함
        // 또는 이 메서드 내에서 직접 장르 보강 로직을 수행해야 함
        List<MovieResponseDto> allMovies = fetchAllDailyBoxOfficeMoviesWithGenres().movies(); // 변경된 함수 호출
        List<MovieResponseDto> filteredMovies = filterMoviesByMovieName(allMovies, tags);

        if (filteredMovies.isEmpty()) {
            throw new BusinessException(ErrorCode.MOVIE_API_NO_RESULT, ErrorCode.MOVIE_API_NO_RESULT.getMessage());
        }

        return new MovieListResponseDto(filteredMovies);
    }

    // 영화 목록에서 영화명(movieNm)에 태그가 포함된 영화를 필터링
    private List<MovieResponseDto> filterMoviesByMovieName(List<MovieResponseDto> movies, List<String> tags) {
        return movies.stream()
                // 각 영화(MovieResponseDto)에 대해 필터링 조건 적용
                .filter(movie ->
                        // 추천된 태그 중 하나라도 movieNm에 포함되어 있는지 확인
                        tags.stream().anyMatch(tag -> {
                            // movieNm이 null일 수 있으므로 Optional로 처리하여 빈 문자열로 대체
                            String movieName = Optional.ofNullable(movie.movieNm()).orElse("");
                            // movieNm에 현재 태그가 포함되어 있는 경우 true
                            return movieName.contains(tag);
                        })
                )
                // 최대 3개의 영화만 선택
                .limit(3)
                // 결과를 리스트로 변환
                .toList();
    }
}
