package io.github.alstn113.assignment.infra.client.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;
import io.github.alstn113.assignment.infra.client.IpInfoHttpClient;
import io.github.alstn113.assignment.infra.client.exception.IpInfoNonRetryableException;
import io.github.alstn113.assignment.infra.client.exception.IpInfoRetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class IpInfoClientConfig {

    private final IpInfoProperties properties;

    @Bean
    public IpInfoHttpClient ipInfoHttpClient(
            RestClient.Builder builder,
            IpInfoProperties properties
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(properties.readTimeout());
        factory.setConnectTimeout(properties.connectionTimeout());

        RestClient restClient = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .defaultStatusHandler(HttpStatusCode::isError, errorHandler())
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factoryProxy = HttpServiceProxyFactory.builderFor(adapter).build();

        return factoryProxy.createClient(IpInfoHttpClient.class);
    }

    private static ErrorHandler errorHandler() {
        return (request, response) -> {
            HttpStatusCode status = response.getStatusCode();
            if (status == HttpStatus.TOO_MANY_REQUESTS || status.is5xxServerError()) {
                throw new IpInfoRetryableException("API 일시적 오류: " + status);
            }
            if (status.is4xxClientError()) {
                throw new IpInfoNonRetryableException("클라이언트 오류: " + status);
            }
        };
    }

    @Bean
    public Cache<String, IpInfo> ipInfoCache() {
        return Caffeine.newBuilder()
                .maximumSize(properties.cache().maxSize())
                .expireAfterWrite(properties.cache().ttl())
                .build();
    }
}
