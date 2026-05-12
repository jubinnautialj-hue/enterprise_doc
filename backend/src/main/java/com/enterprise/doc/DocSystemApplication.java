package com.enterprise.doc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.enterprise.doc.mapper")
public class DocSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocSystemApplication.class, args);
    }
}
