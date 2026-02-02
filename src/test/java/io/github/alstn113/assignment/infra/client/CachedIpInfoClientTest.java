package io.github.alstn113.assignment.infra.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.alstn113.assignment.application.IpInfoClient;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachedIpInfoClientTest {

    @Mock
    private IpInfoClient delegate;

    private CachedIpInfoClient cachedClient;
    private Cache<String, IpInfo> cache;

    @BeforeEach
    void setUp() {
        cache = Caffeine.newBuilder().build();
        cachedClient = new CachedIpInfoClient(delegate, cache);
    }

    @Test
    @DisplayName("처음 조회할 때는 delegate 를 호출하고 결과를 캐시한다")
    void fetchAndCache() {
        // given
        String ip = "1.1.1.1";
        IpInfo expected = new IpInfo(ip, "KR", "Seoul", "Seoul", "AS123");
        when(delegate.fetchIpInfo(ip)).thenReturn(expected);

        // when
        IpInfo result1 = cachedClient.fetchIpInfo(ip);
        IpInfo result2 = cachedClient.fetchIpInfo(ip);

        // then
        assertThat(result1).isEqualTo(expected);
        assertThat(result2).isEqualTo(expected);
        verify(delegate, times(1)).fetchIpInfo(anyString()); // 한 번만 호출됨
    }
}
