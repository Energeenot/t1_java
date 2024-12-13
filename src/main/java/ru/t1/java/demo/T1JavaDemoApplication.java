package ru.t1.java.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@EnableJpaRepositories(basePackages = "ru.t1.java.demo.repository")
@EntityScan(basePackages = "ru.t1.java.demo.model")
@EnableScheduling
public class T1JavaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(T1JavaDemoApplication.class, args);
    }

}
