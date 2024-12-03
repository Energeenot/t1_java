//package ru.t1.java.demo.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import ru.t1.java.demo.model.DataSourceErrorLog;
//import ru.t1.java.demo.repository.DataSourceErrorLogRepository;
//
//@Service
//public class ErrorLogService {
//
//    private final DataSourceErrorLogRepository errorLogRepository;
//
//    @Autowired
//    public ErrorLogService(DataSourceErrorLogRepository errorLogRepository) {
//        this.errorLogRepository = errorLogRepository;
//    }
//
////    public void saveErrorLog(String stackTrace, String message, String methodSignature) {
////        DataSourceErrorLog log = DataSourceErrorLog.builder()
////                .stackTrace(stackTrace)
////                .message(message)
////                .methodSignature(methodSignature)
////                .build();
////        errorLogRepository.save(log);
////    }
//
//    public void saveErrorLog(DataSourceErrorLog log) {
//        errorLogRepository.save(log);
//    }
//}
