package com.lhh.serveradmin.mqtt;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanProjectEntity;
import com.lhh.serverbase.utils.CopyUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ProjectSender {

    @Value("${mqtt-setting.host}")
    private String host;
    @Value("${mqtt-setting.port}")
    private Integer port;
    @Value("${mqtt-setting.userName}")
    private String userName;
    @Value("${mqtt-setting.password}")
    private String password;
    @Value("${mqtt-setting.pub-topic}")
    private String pubTopic;
    @Value("${mqtt-setting.sub-num}")
    private Integer subNum;

    @Autowired
    AmqpTemplate amqpTemplate;

    public void sendToMqtt(ScanProjectEntity project) {
        if (CollectionUtils.isEmpty(project.getHostList())) {
            return ;
        }
        List<ScanProjectEntity> list = splitList(project, subNum);
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

        Connection connection = null;
        Channel channel = null;
        try {
            //2. 创建连接Connection
            connection = connectionFactory.newConnection("生产者");
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
            channel.queueDeclare(pubTopic, true, false, false, null);
            //6. 将消息发送到队列
            for (ScanProjectEntity p : list) {
                channel.basicPublish("", pubTopic, null, SerializationUtils.serialize(p));
            }
            log.info("项目" + project.getId() + "推送mq成功");
        } catch (Exception e) {
            log.error("项目" + project.getId() + "推送mq失败");
            e.printStackTrace();
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
    }

    public void sendToMqtt2(ScanProjectEntity project) {
        if (CollectionUtils.isEmpty(project.getHostList())) {
            return ;
        }
//        List<ScanProjectEntity> list = splitList(project, subNum);
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

        Connection connection = null;
        Channel channel = null;
        try {
            //2. 创建连接Connection
            connection = connectionFactory.newConnection("生产者");
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
            channel.queueDeclare(pubTopic, true, false, false, null);
            //6. 将消息发送到队列
            List<ScanProjectEntity> list = new ArrayList<>();
            for (int i =0;i<10;i++) {
                ScanProjectEntity p = ScanProjectEntity.builder().queueId(String.valueOf(i)).build();
                List<String> l = new ArrayList<>();
                for (int j =0;j<100;j++) {
                    l.add(String.valueOf(i*100+j));
                }
                p.setHostList(l);
                list.add(p);
            }
            for (ScanProjectEntity p : list) {
                channel.basicPublish("", pubTopic, null, SerializationUtils.serialize(p));
            }
            log.info("项目" + project.getId() + "推送mq成功");
        } catch (Exception e) {
            log.error("项目" + project.getId() + "推送mq失败");
            e.printStackTrace();
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
    }

    public static List<ScanProjectEntity> splitList(ScanProjectEntity project, int len) {
        List<String> list = project.getHostList();
        if (list == null || list.isEmpty() || len < 1) {
            return Collections.emptyList();
        }
        List<ScanProjectEntity> result = new ArrayList<>();
        int size = list.size();
        int count = (size + len - 1) / len;
        for (int i = 0; i < count; i++) {
            ScanProjectEntity subProject = new ScanProjectEntity();
            CopyUtils.copyProperties(project, subProject);
            subProject.setQueueId(project.getId() + Const.STR_UNDERLINE + count + Const.STR_UNDERLINE + (i+1));
            List<String> subList = list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)));
            subProject.setHostList(new ArrayList<>(subList));
            result.add(subProject);
        }
        return result;
    }

}
