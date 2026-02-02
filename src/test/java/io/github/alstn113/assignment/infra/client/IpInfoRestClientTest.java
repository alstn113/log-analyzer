package io.github.alstn113.assignment.infra.client;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;
import io.github.alstn113.assignment.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class IpInfoRestClientTest extends AbstractIntegrationTest {

    @Autowired
    private IpInfoRestClient ipInfoRestClient;

    @Test
    @Disabled("실제 API 호출 테스트이므로 성공 확인 후 비활성화")
    @DisplayName("실제 ipinfo API 를 호출하여 정보를 가져온다")
    void fetchRealIpInfo() {
        // given
        String ip = "8.8.8.8";

        // when
        IpInfo result = ipInfoRestClient.fetchIpInfo(ip);

        // then
        assertThat(result.ip()).isEqualTo(ip);
        // 토큰이 없더라도 Google DNS 여서 관련 정보가 올 가능성이 높음
        // 혹은 Rate Limit 에 걸리면 UNKNOWN 이 올 수 있음 (Fallback 처리됨)
        assertThat(result.country()).isNotEqualTo("UNKNOWN");
    }
}
