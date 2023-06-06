package com.lhh.servermonitor.sync;

import com.lhh.serverbase.dto.ScanParamDto;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class SyncService {

    @Value(value = "${sync-setting.MAX_THREADS}")
    private Integer MAX_THREADS;
    @Value(value = "${sync-setting.EXPIRED_PAGE_SIZE}")
    private Integer EXPIRED_PAGE_SIZE;

    /*public void dataHandler(List<ScanParamDto> list, Message message, Channel channel) {
        //处理数据数量
        int listSize = list.size();
        //线程数
        int runSize;
        if (listSize % EXPIRED_PAGE_SIZE == 0) {
            runSize = (listSize / EXPIRED_PAGE_SIZE);
        } else {
            runSize = (listSize / EXPIRED_PAGE_SIZE) + 1;
        }
        ThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(runSize);
        CountDownLatch countDownLatch = new CountDownLatch(runSize);
        //最大并发线程数控制
        final Semaphore semaphore = new Semaphore(MAX_THREADS);
        List handleList = null;
        for (int i = 0; i < runSize; i++) {
            if ((i + 1) == runSize) {
                int startIndex = i * EXPIRED_PAGE_SIZE;
                int endIndex = list.size();
                handleList = list.subList(startIndex, endIndex);
            } else {
                int startIndex = i * EXPIRED_PAGE_SIZE;
                int endIndex = (i + 1) * EXPIRED_PAGE_SIZE;
                handleList = list.subList(startIndex, endIndex);
            }
            SyncTask task = new SyncTask(handleList, countDownLatch, semaphore, message, channel);
            executor.execute(task);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally{
            executor.shutdown();
        }
    }*/

    public void dataHandler(List<ScanParamDto> list, Message message, Channel channel) {
        //处理数据数量
        int listSize = list.size();
        ThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(listSize);
        CountDownLatch countDownLatch = new CountDownLatch(listSize);
        //最大并发线程数控制
        final Semaphore semaphore = new Semaphore(MAX_THREADS);
        SyncTask task = new SyncTask(list, countDownLatch, semaphore, message, channel);
        executor.execute(task);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally{
            executor.shutdown();
        }
    }

}
