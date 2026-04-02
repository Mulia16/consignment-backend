package com.consignment.service;

import com.consignment.service.config.QueueProperties;
import com.consignment.service.config.SettlementBatchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.consignment.service.persistence.mapper")
@EnableConfigurationProperties({QueueProperties.class, SettlementBatchProperties.class})
@EnableScheduling
public class ConsignmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsignmentServiceApplication.class, args);
    }
}
