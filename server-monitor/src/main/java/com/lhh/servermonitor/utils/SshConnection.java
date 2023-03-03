package com.lhh.servermonitor.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Data
@Configuration
@ConfigurationProperties(prefix = "server-setting")
public class SshConnection {

    private static String hostName;
    private static String userName;
    private static String pwd;
    private static Long execTimeout;
    @Value("${server-setting.host_name}")
    private String hostNameY;
    @PostConstruct
    public void setHostName() {
        hostName=this.hostNameY;
    }
    @Value("${server-setting.user_name}")
    private String userNameY;
    @PostConstruct
    public void setUserName(){
        userName = userNameY;
    }
    @Value("${server-setting.pwd}")
    private String pwdY;
    @PostConstruct
    public void setPwd(){
        pwd = pwdY;
    }
    @Value("${server-setting.exec_timeout}")
    private Long timeoutY;
    @PostConstruct
    public void setTimeout(){
        execTimeout = timeoutY;
    }

    public static String getHostName() {
        return hostName;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPwd() {
        return pwd;
    }

    public static Long getTimeout() {
        return execTimeout;
    }
}
