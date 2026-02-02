package io.github.alstn113.assignment.infra.client;

import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;

public record IpInfoResponse(
        String ip,
        String city,
        String region,
        String country,
        String loc,
        String org,
        String postal,
        String timezone
) {

    public IpInfo toIpInfo() {
        return new IpInfo(
                ip,
                IpInfo.markAsUnknownIfAbsent(country),
                IpInfo.markAsUnknownIfAbsent(region),
                IpInfo.markAsUnknownIfAbsent(city),
                IpInfo.markAsUnknownIfAbsent(org)
        );
    }
}
