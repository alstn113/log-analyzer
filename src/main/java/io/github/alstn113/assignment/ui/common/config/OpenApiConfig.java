package io.github.alstn113.assignment.ui.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Log Analysis API")
                .version("1.0.0")
                .description("""
                        ## Log Analysis API
                        
                        접속 로그 CSV 파일을 분석하여 통계 정보를 제공하는 애플리케이션입니다.
                        
                        ### 주요 기능
                        - 로그 파일 업로드 및 분석
                        - TopN의 경로, 상태 코드, IP 조회
                        - IP 위치 정보 enrichment (최대 100개)
                        - 상태 코드 비율 통계
                        
                        ### 지원 형식
                        - 최대 파일 크기: 50MB
                        - 최대 라인 수: 200,000줄
                        - topN 범위: 1-100
                        """);

        return new OpenAPI()
                .info(info);
    }
}
