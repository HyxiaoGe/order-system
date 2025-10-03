package com.hyxiao.procurement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 采购服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.hyxiao.procurement", "com.hyxiao.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class ProcurementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcurementServiceApplication.class, args);
    }
}