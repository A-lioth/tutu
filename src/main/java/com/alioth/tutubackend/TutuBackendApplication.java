package com.alioth.tutubackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@MapperScan("com.alioth.tutubackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class TutuBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutuBackendApplication.class, args);
    }

}
