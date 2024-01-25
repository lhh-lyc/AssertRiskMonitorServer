package com.lhh.serveradmin.task;

import com.lhh.serveradmin.service.TaskService;
import com.lhh.serverbase.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class StatisticsNumTask {

    @Autowired
    TaskService taskService;

    /**
     * 统计项目端口、url数量
     * @return
     */
    @Scheduled(cron = "0 0/10 * * * ? ")
    @GetMapping("statisticsProjectNum")
    public R statisticsProjectNum() {
        log.info("statisticsProjectNum定时任务开始");
        taskService.statisticsProjectNum();
        return R.ok();
    }

}
