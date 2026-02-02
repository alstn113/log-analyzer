package io.github.alstn113.assignment.ui.common.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "ipInfoExecutor")
    public Executor ipInfoExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // 기본 스레드 20개
        executor.setMaxPoolSize(50); // 최대 스레드 50개
        executor.setQueueCapacity(500); // 큐 대기 500개
        executor.setThreadNamePrefix("ip-info-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 거부 시 호출한 스레드가 실행
        executor.initialize();
        return executor;
    }
}
