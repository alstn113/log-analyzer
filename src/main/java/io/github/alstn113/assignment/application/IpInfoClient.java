package io.github.alstn113.assignment.application;

import io.github.alstn113.assignment.domain.analysis.vo.LogStatistics.IpInfo;

public interface IpInfoClient {

    IpInfo fetchIpInfo(String ip);
}
