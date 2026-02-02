package io.github.alstn113.assignment.domain.analysis.vo;

import java.util.List;

public record LogStatistics(
        long totalRequests,
        StatusCodeStats statusCodeDistribution,
        List<PathCount> topPaths,
        List<StatusCodeCount> topStatusCodes,
        List<IpCount> topIps
) {

    public record StatusCodeStats(
            double success2xx,
            double redirect3xx,
            double clientError4xx,
            double serverError5xx
    ) {
    }

    public record PathCount(String path, long count) {
    }

    public record StatusCodeCount(int statusCode, long count) {
    }

    public record IpCount(
            String ip,
            long count,
            IpInfo ipInfo
    ) {
    }

    public record IpInfo(
            String ip,
            String country,
            String region,
            String city,
            String org
    ) {

        public static IpInfo unknown(String ip) {
            return new IpInfo(ip, "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN");
        }

        public static String markAsUnknownIfAbsent(String value) {
            if (value == null || value.isBlank()) {
                return "UNKNOWN";
            }
            return value;
        }
    }
}
