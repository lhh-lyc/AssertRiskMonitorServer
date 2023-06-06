package com.lhh.servermonitor.sync;

import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.servermonitor.service.ScanPortInfoService;
import com.lhh.servermonitor.utils.BeanContextUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

@Slf4j
public class SyncTask implements Runnable {

    private ScanPortInfoService scanPortInfoService;

    private List<ScanParamDto> list;
    private CountDownLatch countDownLatch;
    private Semaphore semaphore;
    private Message message;
    private Channel channel;

    public SyncTask(List<ScanParamDto> list, CountDownLatch countDownLatch, Semaphore semaphore, Message message, Channel channel) {
        this.list = list;
        this.countDownLatch = countDownLatch;
        this.semaphore = semaphore;
        this.message = message;
        this.channel = channel;
    }

    @Override
    public void run() {
        this.scanPortInfoService = BeanContextUtil.getApplicationContext().getBean(ScanPortInfoService.class);
        if (!CollectionUtils.isEmpty(list)) {
            try {
                semaphore.acquire();
                list.stream().forEach(dto -> {
                    //业务处理
                    scanPortInfoService.scanSingleIpPortList(dto);
                });
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, true);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } finally {
                semaphore.release();
            }
        }
        //线程任务完成
        countDownLatch.countDown();
    }

}
