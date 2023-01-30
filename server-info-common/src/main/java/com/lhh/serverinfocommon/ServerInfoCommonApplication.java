package com.lhh.serverinfocommon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ServerInfoCommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerInfoCommonApplication.class, args);
    }

}
