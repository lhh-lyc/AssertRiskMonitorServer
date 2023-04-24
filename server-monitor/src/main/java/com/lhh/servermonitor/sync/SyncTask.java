package com.lhh.servermonitor.sync;

import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.servermonitor.service.ScanPortInfoService;
import com.lhh.servermonitor.utils.BeanContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

@Slf4j
public class SyncTask implements Runnable {

    private ScanPortInfoService scanPortInfoService;

    private List<ScanParamDto> list;
    private CountDownLatch countDownLatch;
    private Semaphore semaphore;

    public SyncTask(List<ScanParamDto> list, CountDownLatch countDownLatch, Semaphore semaphore) {
        this.list = list;
        this.countDownLatch = countDownLatch;
        this.semaphore = semaphore;
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }
        //线程任务完成
        countDownLatch.countDown();
    }

}
