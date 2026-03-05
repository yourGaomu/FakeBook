package com.zhangzc.thredlocalpoolspringbootstart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class ThreadPoolConfig {

    // 核心线程数（优先取配置值，无配置则用CPU核心数）
    @Value("${thread.pool.core-size:-1}") // 默认值设为-1，表示未配置
    private int corePoolSize;

    // 最大线程数（优先取配置值，无配置则用核心数*2）
    @Value("${thread.pool.max-size:-1}") // 默认值设为-1，表示未配置
    private int maxPoolSize;

    // 队列容量（默认1000）
    @Value("${thread.pool.queue-capacity:1000}")
    private int queueCapacity;

    // 空闲线程存活时间（秒）
    @Value("${thread.pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    // 线程名称前缀（便于日志排查）
    @Value("${thread.pool.name-prefix:biz-thread-}")
    private String threadNamePrefix;

    /**
     * 核心业务线程池
     */
    @Bean(name = "threadPoolTaskExecutor")
    @Primary
    public ThreadPoolTaskExecutor bizThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 处理核心线程数：未配置则用CPU核心数
        int cpuCoreNum = Runtime.getRuntime().availableProcessors();
        if (corePoolSize == -1) {
            corePoolSize = cpuCoreNum;
        }

        // 2. 处理最大线程数：未配置则用核心数*2
        if (maxPoolSize == -1) {
            maxPoolSize = corePoolSize * 2;
        }

        // 3. 校验参数合法性（避免配置错误）
        if (corePoolSize <= 0) {
            corePoolSize = 1; // 兜底：至少1个核心线程
            log.warn("核心线程数配置非法，兜底设为1");
        }
        if (maxPoolSize < corePoolSize) {
            maxPoolSize = corePoolSize * 2; // 兜底：最大数不小于核心数
            log.warn("最大线程数小于核心线程数，自动调整为{}", maxPoolSize);
        }

        // 线程池核心配置
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(threadNamePrefix);

        // 拒绝策略 + 优雅关闭
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // 初始化
        executor.initialize();

        // 注册监控
        monitorThreadPool(executor);

        log.info("线程池初始化完成 | 核心数：{} | 最大数：{} | 队列容量：{}",
                corePoolSize, maxPoolSize, queueCapacity);
        return executor;
    }

    /**
     * 线程池监控：打印核心参数
     */
    public void monitorThreadPool(ThreadPoolTaskExecutor executor) {
        ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();

        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(30);
                    log.info("===== 线程池监控 ======");
                    log.info("核心线程数：{}", threadPoolExecutor.getCorePoolSize());
                    log.info("活跃线程数：{}", threadPoolExecutor.getActiveCount());
                    log.info("最大线程数：{}", threadPoolExecutor.getMaximumPoolSize());
                    log.info("队列任务数：{}", threadPoolExecutor.getQueue().size());
                    log.info("已完成任务数：{}", threadPoolExecutor.getCompletedTaskCount());
                    log.info("总任务数：{}", threadPoolExecutor.getTaskCount());
                    log.info("======================");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("线程池监控线程被中断", e);
                    break;
                }
            }
        }, "thread-pool-monitor").setDaemon(true);
    }
}