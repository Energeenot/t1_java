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
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;
import ru.t1.java.demo.service.ErrorLogService;

@Aspect
@Component
@Async
@Slf4j
public class DataSourceErrorAspect {

    private final ErrorLogService errorLogService;

    @Autowired
    public DataSourceErrorAspect(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @Pointcut("@annotation(ru.t1.java.demo.aop.LogDataSourceError)")
    public void logDataSourceError() {}

    @AfterThrowing(pointcut = "logDataSourceError()", throwing = "ex")
    public void logDataSourceError(JoinPoint joinPoint, Throwable ex) {
        String methodSignature = joinPoint.getSignature().toShortString();
        String stackTrace = ExceptionUtils.getStackTrace(ex);

        log.error("Exception in method {}", methodSignature);
        errorLogService.saveErrorLog(stackTrace, ex.getMessage(), methodSignature);
    }
}
