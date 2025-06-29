package com.likelion.basecode.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    // 스프링 컨테이너에 의해 관리되는 재사용 가능한 소프트웨어 컴포넌트
    @Bean
    // RestTemplate 빈을 생성해 스프링 컨테이너에 등록
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
