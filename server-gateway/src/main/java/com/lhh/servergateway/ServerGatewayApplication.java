package com.lhh.servergateway;

import com.lhh.servergateway.filter.AuthFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServerGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerGatewayApplication.class, args);
    }

    @Bean
    public AuthFilter getAuthFilter(){
        return new AuthFilter();
    }

}
