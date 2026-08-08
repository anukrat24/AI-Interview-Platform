package com.aiprep.interview.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Explicit @Async configuration.
 *
 * Without a bean named "taskExecutor" (or an AsyncConfigurer), Spring falls back to a
 * SimpleAsyncTaskExecutor that creates a new thread per task and swallows any exceptions
 * thrown from void-returning @Async methods (like EmailService.sendOtpEmail), which is why
 * failures there can go completely unlogged. Defining a real TaskExecutor plus an
 * AsyncUncaughtExceptionHandler ensures async tasks actually run on a managed pool and that
 * any exception is logged instead of disappearing silently.
 */
@Configuration
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        log.debug("AsyncConfig: taskExecutor initialized (core={}, max={}, queueCapacity={})",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable throwable, Method method, Object... params) -> {
            log.error("Uncaught exception in async method '{}' with params {}: {}",
                    method.getName(), params, throwable.getMessage(), throwable);
        };
    }
}
