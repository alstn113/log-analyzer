package io.github.alstn113.assignment.infra.client.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.ipinfo")
public record IpInfoProperties(
        String token,
        String baseUrl,
        Duration connectionTimeout,
        Duration readTimeout,
        Cache cache
) {

    public record Cache(
            int maxSize,
            Duration ttl
    ) {
    }
}
