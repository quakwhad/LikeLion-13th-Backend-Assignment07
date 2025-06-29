package com.likelion.basecode.common.client;

import com.likelion.basecode.common.error.ErrorCode;
import com.likelion.basecode.common.exception.BusinessException;
import com.likelion.basecode.movie.api.dto.response.MovieInfoResponseDto;
import com.likelion.basecode.movie.api.dto.response.MovieResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper; // ObjectMapper 추가
import com.fasterxml.jackson.databind.SerializationFeature; // SerializationFeature 추가 (JSON 예쁘게 출력용)

@Component
@RequiredArgsConstructor
public class MovieSearchClient {

    private final RestTemplate restTemplate;

    // ObjectMapper는 JSON 직렬화/역직렬화에 사용됩니다.
    // 여기서는 응답 Map을 예쁘게 출력하기 위해 사용합니다.
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);


    @Value("${movie-api.boxOfficeUrl}")
    private String boxOfficeUrl;

    @Value("${movie-api.movieInfoUrl}")
    private String movieInfoUrl;

    @Value("${movie-api.service-key}")
    private String serviceKey;

    // 외부 영화 API로부터 전체 영화 목록을 조회
    public List<MovieResponseDto> fetchDailyBoxOffice(String targetDate) {
        URI uri = UriComponentsBuilder.fromUriString(boxOfficeUrl)
                .queryParam("key", serviceKey)
                .queryParam("targetDt", targetDate) // 조회할 날짜 파라미터 추가 (YYYYMMDD)
                // 필수 파라미터는 2개뿐이라 2개만 입력받았습니다.
                // .queryParam("itemPerPage", 10) // Optional: 한 페이지 결과 수 (기본값 10)
                // .queryParam("multiMovieCd", "") // Optional: 영화 코드를 ,로 구분해서 여러 개 지정
                // .queryParam("repNationCd", "") // Optional: 대한민국: KR, 그 외: 외
                // .queryParam("wideAreaCd", "") // Optional: 상영지역코드 (공통코드 조회 서비스 참고)
                .build()
                .toUri();

        System.out.println("KOBIS API 요청 URI: " + uri); // 디버깅을 위한 URI 출력

        // 외부 API 호출
        ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);

        // --- 여기에 원본 JSON 응답 로깅 코드 추가 ---
        if (response.getBody() != null) {
            try {
                // response.getBody()는 이미 Map 형태로 파싱된 상태입니다.
                // 이를 다시 JSON 문자열로 변환하여 예쁘게 출력합니다.
                String originalJsonResponse = objectMapper.writeValueAsString(response.getBody());
                System.out.println("--- KOBIS API Original JSON Response ---");
                System.out.println(originalJsonResponse);
                System.out.println("----------------------------------------");
            } catch (Exception e) {
                System.err.println("Failed to print original JSON response: " + e.getMessage());
            }
        }

        // 응답 body가 null인 경우 예외 발생
        Map<String, Object> body = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new BusinessException(ErrorCode.MOVIE_API_RESPONSE_NULL, ErrorCode.MOVIE_API_RESPONSE_NULL.getMessage()));

        // KOBIS API 응답 구조에 맞게 'item' 리스트를 추출
        // 'item' 리스트를 추출한 후 movieResponseDto 형태로 변환
        return extractItemList(body).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 응답 맵에서 영화 항목 리스트 추출
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItemList(Map<String, Object> responseMap) {
        Map<String, Object> boxOfficeResult = castToMap(responseMap.get("boxOfficeResult"), ErrorCode.MOVIE_API_BODY_MALFORMED);
        Object dailyBoxOfficeList = boxOfficeResult.get("dailyBoxOfficeList");

        if (dailyBoxOfficeList instanceof List<?> itemList) {
            return (List<Map<String, Object>>) itemList;
        }

        // 그 외는 형식 오류로 예외 처리
        throw new BusinessException(ErrorCode.MOVIE_API_ITEM_MALFORMED, ErrorCode.MOVIE_API_ITEM_MALFORMED.getMessage());
    }

    // 개별 영화 항목 Map을 MovieResponseDto로 변환 (요청하신 필드만 매핑)
    private MovieResponseDto toDto(Map<String, Object> item) {
        return new MovieResponseDto(
                (String) item.getOrDefault("movieCd", ""),      // 영화명(국문)
                (String) item.getOrDefault("movieNm", ""),      // 영화명(국문)
                (String) item.getOrDefault("openDt", ""),       // 개봉일
                (String) item.getOrDefault("repNationNm", ""),  // 대표 제작국가명
                (String) item.getOrDefault("genreAlt", "")    // repGenreNm 대신 genreAlt로 변경
        );
    }

    public MovieInfoResponseDto fetchMovieDetail(String movieCd) {
        URI uri = UriComponentsBuilder.fromUriString(movieInfoUrl) // 새로운 @Value 필드 필요
                .queryParam("key", serviceKey)
                .queryParam("movieCd", movieCd)
                .build()
                .toUri();

        ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);
        Map<String, Object> body = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new BusinessException(ErrorCode.MOVIE_API_RESPONSE_NULL, "영화 상세 정보 응답이 null입니다."));

        // 상세 정보 응답 구조 파싱 (movieInfoResult -> movieInfo -> genres)
        Map<String, Object> movieInfoResult = castToMap(body.get("movieInfoResult"), ErrorCode.MOVIE_API_BODY_MALFORMED);
        Map<String, Object> movieInfo = castToMap(movieInfoResult.get("movieInfo"), ErrorCode.MOVIE_API_BODY_MALFORMED);

        // genres는 리스트 형태이며, 각 요소는 Map{"genreNm":"장르명"} 입니다.
        List<Map<String, Object>> genres = (List<Map<String, Object>>) movieInfo.getOrDefault("genres", List.of());
        String genreAlt = genres.stream()
                .map(g -> g.getOrDefault("genreNm", "").toString())
                .collect(Collectors.joining(", ")); // 여러 장르인 경우 콤마로 연결

        return new MovieInfoResponseDto(genreAlt);
    }

    // 위에서 설명한 상황과 유사하다고 생각하면 됨. (정확히는 Map<String, Object> 캐스팅은 컴파일러가 타입 안정성을 확인할 수 없기 때문)
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object obj, ErrorCode errorCode) {
        // obj가 Map 타입이 아닌 경우 예외를 발생
        if (!(obj instanceof Map)) {
            // 비즈니스 로직에 정의된 에러 코드와 메시지를 포함한 예외를 던짐
            throw new BusinessException(errorCode, errorCode.getMessage());
        }

        // Map 타입이 확인 -> 따라서 안전하게 형변환하여 반환-!
        return (Map<String, Object>) obj;
    }
}
