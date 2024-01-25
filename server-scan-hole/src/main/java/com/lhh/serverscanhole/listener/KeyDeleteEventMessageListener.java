package com.lhh.serverscanhole.listener;

import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverscanhole.controller.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class KeyDeleteEventMessageListener extends KeyspaceEventMessageListener implements ApplicationEventPublisherAware {

    @Autowired
    RedisLock redisLock;

    private static final Topic KEYEVENT_DELETE_TOPIC = new PatternTopic("__keyevent@*__:del");
    @Nullable
    private ApplicationEventPublisher publisher;
 
    public KeyDeleteEventMessageListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    protected void doRegister(RedisMessageListenerContainer listenerContainer) {
        listenerContainer.addMessageListener(this, KEYEVENT_DELETE_TOPIC);
    }
 
    @Override
    protected void doHandleMessage(Message message) {
        /*String expiredKey = message.toString();
        if (!StringUtils.isEmpty(expiredKey) && expiredKey.contains(CacheConst.REDIS_SCANNING_DOMAIN)) {
            String[] list = expiredKey.split(Const.STR_COLON);
            Long projectId = Long.valueOf(list[1]);
            String domain = list[2];
            redisLock.removeProjectRedis(projectId, domain);
            log.info("主域名扫描完成，key:" + expiredKey);
        }*/
        this.publishEvent(new RedisKeyExpiredEvent(message.getBody()));
    }
 
    protected void publishEvent(RedisKeyExpiredEvent event) {
        if (this.publisher != null) {
            this.publisher.publishEvent(event);
        }
 
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}