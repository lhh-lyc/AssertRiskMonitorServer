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
public class ProjectCheckTask {

    @Autowired
    TaskService taskService;

    /**
     * REDIS_SCANNING_PROJECT 未执行完的任务
     * mysql里面的ip,全部不在redis里面就算做执行完成
     * @return
     */
//    @Scheduled(cron = "0 0/10 * * * ? ")
    @GetMapping("checkProject")
    public R checkProject() {
        log.info("checkProject定时任务开始");
        taskService.checkProject();
        return R.ok();
    }

}
