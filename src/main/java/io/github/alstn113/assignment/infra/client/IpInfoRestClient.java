package io.github.alstn113.assignment.infra.client;

import io.github.alstn113.assignment.application.IpInfoClient;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;
import io.github.alstn113.assignment.infra.client.config.IpInfoProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("ipInfoRestClient")
@RequiredArgsConstructor
public class IpInfoRestClient implements IpInfoClient {

    private final IpInfoHttpClient httpClient;
    private final IpInfoProperties properties;

    @Override
    @CircuitBreaker(name = "ipInfoApi", fallbackMethod = "getFallback")
    @Retry(name = "ipInfoApi")
    @RateLimiter(name = "ipInfoApi")
    public IpInfo fetchIpInfo(String ip) {
        IpInfoResponse response = httpClient.getIpInfo(ip, properties.token());

        return response.toIpInfo();
    }

    private IpInfo getFallback(String ip, Exception e) {
        return IpInfo.unknown(ip);
    }
}
