package com.lhh.servermonitor.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.ConsumerTagStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAMQPConsumerConfig {

    @Value("${mqtt-setting.host}")
    private String host;
    @Value("${mqtt-setting.port}")
    private Integer port;
    @Value("${mqtt-setting.userName}")
    private String userName;
    @Value("${mqtt-setting.password}")
    private String password;
    @Value(value = "${mqtt-setting.concurrent_consumers}")
    private Integer concurrentConsumers;
    @Value(value = "${mqtt-setting.maxConcurrent_consumers}")
    private Integer maxConcurrentConsumers;
    @Value(value = "${mqtt-setting.prefetch_count}")
    private Integer prefetchCount;


    @Bean
    public ConnectionFactory connectionFactory() {
        com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();

        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);

        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setNetworkRecoveryInterval(10000);
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
        return cachingConnectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory = new SimpleRabbitListenerContainerFactory();
        simpleRabbitListenerContainerFactory.setConnectionFactory(connectionFactory);

        // 设置消费者线程数
        simpleRabbitListenerContainerFactory.setConcurrentConsumers(concurrentConsumers);
        // 设置最大消费者线程数
        simpleRabbitListenerContainerFactory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        // 设置预取数量
        simpleRabbitListenerContainerFactory.setPrefetchCount(prefetchCount);
        // 设置手动确认
        simpleRabbitListenerContainerFactory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        // 设置消费者标签
        simpleRabbitListenerContainerFactory.setConsumerTagStrategy(new ConsumerTagStrategy() {
            @Override
            public String createConsumerTag(String s) {
                return "域名扫描逻辑消费者";
            }
        });
        simpleRabbitListenerContainerFactory.setAutoStartup(true);
        return simpleRabbitListenerContainerFactory;
    }
}
