package io.github.alstn113.assignment.application;

import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;

public interface IpEnrichmentClient {

    IpInfo fetchIpInfo(String ip);
}
