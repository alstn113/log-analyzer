package io.github.alstn113.assignment.application;

import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Slf4j
@Service
public class IpEnrichmentService {

    private final IpInfoClient ipInfoClient;
    private final Executor ipInfoExecutor;

    public IpEnrichmentService(
            IpInfoClient ipInfoClient,
            @Qualifier("ipInfoExecutor") Executor ipInfoExecutor
    ) {
        this.ipInfoClient = ipInfoClient;
        this.ipInfoExecutor = ipInfoExecutor;
    }

    public List<IpCount> enrich(List<IpCount> ips) {
        if (ips.isEmpty()) {
            return List.of();
        }

        StopWatch stopWatch = new StopWatch("IP Enrichment Task");
        stopWatch.start("External API Calls");

        List<IpCount> result = fetchIpInfoInParallel(ips);

        stopWatch.stop();
        log.info("외부 API 요청 완료: 총 {}건, 소요 시간: {}ms", ips.size(), stopWatch.getTotalTimeMillis());

        return result;
    }

    /**
     * 주어진 IP 목록에 대해 병렬로 외부 API 를 호출하여 IP 정보를 조회합니다.
     */
    private List<IpCount> fetchIpInfoInParallel(List<IpCount> ips) {
        List<CompletableFuture<IpCount>> futureResults = ips.stream()
                .map(ip -> CompletableFuture.supplyAsync(() -> fetchAndMap(ip), ipInfoExecutor))
                .toList();

        return futureResults.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private IpCount fetchAndMap(IpCount origin) {
        try {
            IpInfo ipInfo = ipInfoClient.fetchIpInfo(origin.ip());
            return new IpCount(origin.ip(), origin.count(), ipInfo);
        } catch (Exception e) {
            log.warn("IP 조회 실패 [{}]: {}", origin.ip(), e.getMessage());
            return origin;
        }
    }
}