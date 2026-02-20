package io.github.alstn113.assignment.domain.analysis.service;

import io.github.alstn113.assignment.domain.analysis.vo.LogEntry;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.PathCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.StatusCodeCount;
import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.StatusCodeStats;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.stream.Stream;

public class LogAggregator {

    private static final int DEFAULT_TOP_PATHS_N = 100;
    private static final int DEFAULT_TOP_IPS_N = 100;
    private static final int DEFAULT_TOP_STATUS_CODES_N = 100;

    private LogAggregator() {
    }

    /**
     * 로그 엔트리 스트림을 순회하며 통계 정보를 생성한다.
     * 메모리 효율을 위해 스트림을 한 번만 순회하며 모든 통계를 집계한다.
     */
    public static LogStatistics aggregate(Stream<LogEntry> stream) {
        StatsAccumulator accumulator = new StatsAccumulator();
        stream.forEach(accumulator::accumulate);
        return accumulator.build();
    }

    private static class StatsAccumulator {
        private long totalRequests = 0;
        private long success = 0;
        private long redirect = 0;
        private long clientError = 0;
        private long serverError = 0;

        private final Map<String, Long> pathCounts = new HashMap<>();
        private final Map<Integer, Long> statusCodeCounts = new HashMap<>();
        private final Map<String, Long> ipCounts = new HashMap<>();

        void accumulate(LogEntry entry) {
            totalRequests++;

            if (entry.isSuccess()) {
                success++;
            } else if (entry.isRedirect()) {
                redirect++;
            } else if (entry.isClientError()) {
                clientError++;
            } else if (entry.isServerError()) {
                serverError++;
            }

            pathCounts.merge(entry.requestUri(), 1L, Long::sum);
            statusCodeCounts.merge(entry.httpStatus(), 1L, Long::sum);
            ipCounts.merge(entry.clientIp(), 1L, Long::sum);
        }

        LogStatistics build() {
            StatusCodeStats statusCodeStats = new StatusCodeStats(
                    ratio(success, totalRequests),
                    ratio(redirect, totalRequests),
                    ratio(clientError, totalRequests),
                    ratio(serverError, totalRequests));

            List<PathCount> topPaths = topNEntries(pathCounts, DEFAULT_TOP_PATHS_N).stream()
                    .map(e -> new PathCount(e.getKey(), e.getValue()))
                    .toList();
            List<StatusCodeCount> topStatusCodes = topNEntries(statusCodeCounts, DEFAULT_TOP_IPS_N).stream()
                    .map(e -> new StatusCodeCount(e.getKey(), e.getValue()))
                    .toList();
            List<IpCount> topIps = topNEntries(ipCounts, DEFAULT_TOP_STATUS_CODES_N).stream()
                    .map(e -> new IpCount(e.getKey(), e.getValue(), IpInfo.unknown(e.getKey())))
                    .toList();

            return new LogStatistics(
                    totalRequests,
                    statusCodeStats,
                    topPaths,
                    topStatusCodes,
                    topIps);
        }
    }

    /**
     * 부분값이 전체에서 차지하는 비율을 소수점 첫째 자리까지 계산한다.
     */
    private static double ratio(long part, long total) {
        if (total == 0) {
            return 0.0;
        }

        return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 주어진 맵에서 값이 가장 큰 상위 N개의 엔트리를 반환한다.
     */
    private static <K> List<Map.Entry<K, Long>> topNEntries(Map<K, Long> counts, int topN) {
        PriorityQueue<Entry<K, Long>> heap = new PriorityQueue<>(Map.Entry.comparingByValue());

        // 가장 작은 값들을 제거하면서 진행
        for (Map.Entry<K, Long> entry : counts.entrySet()) {
            heap.offer(entry);
            if (heap.size() > topN) {
                heap.poll();
            }
        }

        List<Map.Entry<K, Long>> result = new ArrayList<>(heap);
        result.sort(Map.Entry.<K, Long>comparingByValue().reversed());
        return result;
    }
}
