package io.github.alstn113.assignment.infra.client;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.alstn113.assignment.application.IpInfoClient;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("cachedIpInfoClient")
public class CachedIpInfoClient implements IpInfoClient {

    private final IpInfoClient delegate;
    private final Cache<String, IpInfo> cache;

    public CachedIpInfoClient(
            @Qualifier("ipInfoRestClient") IpInfoClient delegate,
            Cache<String, IpInfo> ipInfoCache
    ) {
        this.delegate = delegate;
        this.cache = ipInfoCache;
    }

    @Override
    public IpInfo fetchIpInfo(String ip) {
        return cache.get(ip, delegate::fetchIpInfo);
    }
}
