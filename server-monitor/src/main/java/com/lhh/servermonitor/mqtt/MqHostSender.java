package com.lhh.servermonitor.mqtt;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class MqHostSender {

    @Value("${mqtt-setting.host}")
    private String host;
    @Value("${mqtt-setting.port}")
    private Integer port;
    @Value("${mqtt-setting.userName}")
    private String userName;
    @Value("${mqtt-setting.password}")
    private String password;
    @Value("${mqtt-setting.scanning-host-pub-topic}")
    private String scanningHostPubTopic;

    @Autowired
    AmqpTemplate amqpTemplate;

    public void sendScanningHostToMqtt(List<ScanParamDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return;
        }
        Boolean reset = false;
        List<ScanParamDto> resetList = new ArrayList<>();
        Integer num = Const.INTEGER_0;
        Connection connection = null;
        Channel channel = null;
        try {
            //2. 创建连接Connection
            connection = getConnect();
            //3. 通过连接获取通道Channel
            channel = connection.createChannel();
            //4. 通过通道创建交换机，声明队列，绑定关系，路由key，发送消息，接收消息
            /**
             * channel.queueDeclare有5个参数
             * params1: 队列的名称
             * params2: 是否要持久化， false：非持久化 true：持久化
             * params3: 排他性，是否独占队列
             * params4: 是否自动删除，如果为true，队列会随着最后一个消费消费完后将队列自动删除，false：消息全部消费完后，队列保留
             * params5: 携带的附加参数
             */
            channel.queueDeclare(scanningHostPubTopic, true, false, false, null);
            channel.confirmSelect();
            //6. 将消息发送到队列
            log.info(dtoList.get(0).getHost() + "本次预计投递数量:" + dtoList.size());
            for (ScanParamDto dto : dtoList) {
                num++;
                log.info(dto.getSubDomain() + "域名开始投递");
                channel.basicPublish("", scanningHostPubTopic, null, SerializationUtils.serialize(dto));
                if (!channel.waitForConfirms()) {
                    log.error("域名投递failed--" + JSON.toJSONString(dto) + ",重新投递！");
                    channel.basicPublish("", scanningHostPubTopic, null, SerializationUtils.serialize(dto));
                } else {
                    log.info("域名投递success--" + JSON.toJSONString(dto));
                }
            }
            log.info(dtoList.get(0).getHost() + "循环投递数量:" + num);
        } catch (Exception e) {
            reset = true;
            resetList.add(dtoList.get(num));
            log.error(dtoList.get(0).getHost() + "推送host-mq产生异常",e);
        } finally {
            //7. 关闭通道
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //8. 关闭连接
            if (connection != null && connection.isOpen()) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (reset) {
            sendScanningHostToMqtt(resetList);
        }
    }

    public Connection getConnect() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //1.1 设置连接IP
        connectionFactory.setHost(host);
        //1.2 设置连接端口
        connectionFactory.setPort(port);
        //1.3 设置用户名
        connectionFactory.setUsername(userName);
        //1.4 设置密码
        connectionFactory.setPassword(password);
        //1.5 设置虚拟访问节点，就是消息发送的目标路径
        connectionFactory.setVirtualHost("/");
        Connection connection = connectionFactory.newConnection("生产者");
        return connection;
    }

}
