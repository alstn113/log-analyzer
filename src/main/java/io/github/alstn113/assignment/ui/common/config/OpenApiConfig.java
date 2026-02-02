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
                        ## 주요 기능
                        - 로그 분석: 업로드된 CSV 파일을 스트리밍 방식으로 읽어 메모리 효율적으로 처리합니다.
                        - 비동기 처리: 로그 분석은 비동기적으로 수행되어 서버의 응답성을 유지합니다.
                        - 통계 요약: 총 요청 수, 성공/실패율, HTTP 상태 코드 분포를 계산합니다.
                        - IP Enrichment: 분석 후 상위 IP 100개에 대해서 외부 IP 정보 서비스와 연동하여 정보를 추가합니다.
                        - 분석 결과 조회: topPaths, topStatusCodes, topIps 를 파라미터로 받아 동적으로 결과를 반환합니다.
                        - IP 정보 캐싱: 외부 서비스 호출 최소화를 위해 IP 정보는 1일간 캐싱됩니다.
                        - 회복 탄력성: 장애에 대비해 Circuit Breaker, Rate Limiter, Retry, Timeout 이 적용되어 있습니다.
                        - IP 정보 요청 병렬 처리: 외부 서비스 호출 시 병렬 처리를 통해 응답 속도를 향상시킵니다.
                        
                        ## 제약 사항
                        - 최대 파일 크기: 50MB
                        - 최대 처리 라인 수: 약 200,000줄
                        - TopN 설정: topPaths, topStatusCodes, topIps 는 1 ~ 100 사이의 값 지원
                        - 데이터 보존: 인메모리 저장소 사용하여 서버 재시작 시 데이터는 소실됨
                        """);

        return new OpenAPI()
                .info(info);
    }
}
