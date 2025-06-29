package com.likelion.basecode.common.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class TagRecommendationClient {
    private final RestTemplate restTemplate;
    private final String apiUrl;

    public TagRecommendationClient(
            RestTemplate restTemplate,
            @Value("${tag.recommendation.api-url}") String apiUrl
    ) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    public List<String> getRecommendedTags(String contents) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("contents", contents);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        Map<String, List<String>> response = restTemplate.postForObject(apiUrl, request, Map.class);

        return Optional.ofNullable(response)
                .map(r -> r.getOrDefault("tags", List.of()))
                .orElse(List.of());
    }
}
