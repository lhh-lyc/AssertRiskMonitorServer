package com.lhh.servermonitor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@Configuration
//@PropertySource("classpath:application.properties")//指定配置文件
public class RabbitMqConfig {
    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${spring.rabbitmq.port}")
    private int port;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${mqtt-setting.exchange}")
    private String exchange;
    @Value("${mqtt-setting.host-route-key}")
    private String hostRouteKey;
    @Value("${mqtt-setting.scanning-host-pub-topic}")
    private String hostTopic;
    @Value("${mqtt-setting.ip-route-key}")
    private String ipRouteKey;
    @Value("${mqtt-setting.scanning-ip-pub-topic}")
    private String ipTopic;

    //创建连接工厂
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host,port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    //必须是prototype类型
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setRetryTemplate(retryTemplate());
        template.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if (ack) {
                    // 如果消息发送成功，则在控制台输出消息ID和发送成功的日志
                    log.info("RabbitTemplate send message success! correlationId: {}", correlationData != null ? correlationData.getId() : null);
                } else {
                    // 如果消息发送失败，则在控制台输出错误信息
                    log.error("RabbitTemplate send message fail! correlationId: {}, error cause: {}", correlationData != null ? correlationData.getId() : null, cause);
                }
            }
        });

        // 设置消息的返回回调函数
        template.setMandatory(true);
        template.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.error("RabbitTemplate message return! message: {}, replyCode: {}, replyText: {}, exchange: {}, routingKey: {}", message, replyCode, replyText, exchange, routingKey);
            }
        });
        return template;
    }

    private RetryTemplate retryTemplate() {
        // 初试时间间隔：500ms，最多重试3次
        RetryTemplate retryTemplate = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(500);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
        return retryTemplate;
    }

    /**
     * * 针对消费者配置
     *      * 1. 设置交换机类型
     *      * 2. 将队列绑定到交换机
     *      FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念
     *      HeadersExchange ：通过添加属性key-value匹配
     *      DirectExchange:按照routingkey分发到指定队列
     *      TopicExchange:多关键字匹配
     */
    //声明交换机
    @Bean
    public DirectExchange defaultExchange() {
        return new DirectExchange(exchange);
    }

    //声明队列
    @Bean
    public Queue queueA() {
        return new Queue(hostTopic, true); //队列持久：不会随着服务器重启造成丢失
    }

    //队列绑定交换机，指定routingkey
    @Bean
    public Binding bindingA() {
        //绑定队列到交换机上通过路由
        return BindingBuilder.bind(queueA()).to(defaultExchange()).with(hostRouteKey);
    }

    //声明队列
    @Bean
    public Queue queueB() {
        return new Queue(ipTopic, true); //队列持久：不会随着服务器重启造成丢失
    }

    //队列绑定交换机，指定routingkey
    @Bean
    public Binding bindingB() {
        //绑定队列到交换机上通过路由
        return BindingBuilder.bind(queueB()).to(defaultExchange()).with(ipRouteKey);
    }

}
