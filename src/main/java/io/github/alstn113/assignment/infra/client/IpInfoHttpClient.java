package io.github.alstn113.assignment.infra.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface IpInfoHttpClient {

    @GetExchange("/{ip}")
    IpInfoResponse getIpInfo(
            @PathVariable("ip") String ip,
            @RequestParam(value = "token", required = false) String token
    );
}
