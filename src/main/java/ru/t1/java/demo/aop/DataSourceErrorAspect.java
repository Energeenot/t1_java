package ru.t1.java.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.kafka.MetricProducer;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.service.ErrorLogService;

import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
@Async
@Slf4j
public class DataSourceErrorAspect {

    private final ErrorLogService errorLogService;
    private final MetricProducer metricProducer;

    @Autowired
    public DataSourceErrorAspect(ErrorLogService errorLogService, MetricProducer metricProducer) {
        this.errorLogService = errorLogService;
        this.metricProducer = metricProducer;
    }

    @Pointcut("@annotation(ru.t1.java.demo.aop.LogDataSourceError)")
    public void logDataSourceError() {}

    @AfterThrowing(pointcut = "logDataSourceError()", throwing = "ex")
    public void logDataSourceError(JoinPoint joinPoint, Throwable ex) {
        String methodSignature = joinPoint.getSignature().toShortString();
        String stackTrace = ExceptionUtils.getStackTrace(ex);
        log.error("Exception in method {}", methodSignature);

        DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                .stackTrace(stackTrace)
                .message(ex.getMessage())
                .methodSignature(methodSignature)
                .build();

        CompletableFuture<SendResult<String, Object>> future = metricProducer.send("t1_demo_metrics", errorLog,
                "errorType", "DATA_SOURCE");
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("Failed to send error log to kafka {}", throwable.getMessage());
                errorLogService.saveErrorLog(errorLog);
            }else log.info("Successfully sent error log to kafka {}", errorLog);
        });
    }
}
