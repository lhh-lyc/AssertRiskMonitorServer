package com.lhh.serveradmin.config;

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

    @Value(value = "${mqtt-setting.concurrent_consumers}")
    private Integer concurrentConsumers;
    @Value(value = "${mqtt-setting.maxConcurrent_consumers}")
    private Integer maxConcurrentConsumers;
    @Value(value = "${mqtt-setting.prefetch_count}")
    private Integer prefetchCount;

    @Bean
    public ConnectionFactory connectionFactory() {
        com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();

        connectionFactory.setHost("49.235.104.19");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("123456");

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

        return simpleRabbitListenerContainerFactory;
    }
}
