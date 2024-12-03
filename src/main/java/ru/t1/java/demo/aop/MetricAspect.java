//package ru.t1.java.demo.aop;
//
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import ru.t1.java.demo.kafka.MetricProducer;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//
//@Aspect
//@Component
//@Slf4j
//@Async
//public class MetricAspect {
//
//    private final MetricProducer metricProducer;
//
//    @Autowired
//    public MetricAspect(MetricProducer metricProducer) {
//        this.metricProducer = metricProducer;
//    }
//
//    @Around("@annotation(metric)")
//    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, Metric metric) throws Throwable {
//        log.info("Executing method {}", joinPoint.getSignature().toShortString());
//        long startTime = System.currentTimeMillis();
//        Object result = null;
//        long duration;
//        try {
//            result = joinPoint.proceed();
//        }finally {
//            long endTime = System.currentTimeMillis();
//            duration = endTime - startTime;
//            log.info("Time taken: {} ms", duration);
//        }
//         if (duration > metric.limit()){
//             Map<String, Object> metrics = new HashMap<>();
//             metrics.put("duration", duration);
//             metrics.put("methodName", joinPoint.getSignature().getName());
//             metrics.put("params", Arrays.toString(joinPoint.getArgs()));
//
//             metricProducer.send("t1_demo_metrics", metrics,
//                     "error_type", "METRICS");
//         }
//
//        return result;
//    }
//}
