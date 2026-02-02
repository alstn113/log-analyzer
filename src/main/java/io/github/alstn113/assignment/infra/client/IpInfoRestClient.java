package io.github.alstn113.assignment.infra.client;

import io.github.alstn113.assignment.application.IpInfoClient;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;
import io.github.alstn113.assignment.infra.client.config.IpInfoProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("ipInfoRestClient")
@RequiredArgsConstructor
public class IpInfoRestClient implements IpInfoClient {

    private final IpInfoHttpClient httpClient;
    private final IpInfoProperties properties;

    @Override
    @Retry(name = "ipInfoApi")
    @CircuitBreaker(name = "ipInfoApi", fallbackMethod = "getFallback")
    @RateLimiter(name = "ipInfoApi")
    public IpInfo fetchIpInfo(String ip) {
        IpInfoResponse response = httpClient.getIpInfo(ip, properties.token());

        return response.toIpInfo();
    }

    private IpInfo getFallback(String ip, Exception e) {
        log.warn("IP 정보 조회 실패 (Fallback 적용) - ip: {}, message: {}", ip, e.getMessage());
        return IpInfo.unknown(ip);
    }
}
