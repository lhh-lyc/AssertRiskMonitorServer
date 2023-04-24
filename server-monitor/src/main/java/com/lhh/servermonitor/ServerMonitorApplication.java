package com.lhh.servermonitor;

import com.lhh.servermonitor.service.InitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableDiscoveryClient
@SpringBootApplication
public class ServerMonitorApplication implements CommandLineRunner {

    @Autowired
    InitService initService;

    public static void main(String[] args) {
        SpringApplication.run(ServerMonitorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        initService.initTask();
    }

}
