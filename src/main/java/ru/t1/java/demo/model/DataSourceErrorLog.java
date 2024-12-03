//package ru.t1.java.demo.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.data.jpa.domain.AbstractPersistable;
//
//@Entity
//@Table(name = "data_source_error_log")
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Setter
//@Getter
//public class DataSourceErrorLog extends AbstractPersistable<Long> {
//
//    @Lob
//    @Column(name = "stackTrace")
//    private String stackTrace;
//
//    @Column(name = "message")
//    private String message;
//
//    @Column(name = "method_signature")
//    private String methodSignature;
//
//
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_source_error_log_seq_gen")
//    @SequenceGenerator(name = "data_source_error_log_seq_gen", sequenceName = "data_source_error_log_seq", allocationSize = 50)
//    public Long getId(){
//        return super.getId();
//    }
//
//}
