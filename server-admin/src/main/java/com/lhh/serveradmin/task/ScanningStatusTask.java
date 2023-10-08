package com.lhh.serveradmin.task;

import com.lhh.serveradmin.service.TaskService;
import com.lhh.serverbase.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ScanningStatusTask {

    @Autowired
    TaskService taskService;

    /**
     * 网络原因导致更新状态失败，重新更新
     * @return
     */
//    @Scheduled(cron = "0 9/10 * * * ? ")
    @GetMapping("scanningChange")
    public R scanningChange() {
        log.info("scanningChange定时任务开始");
        taskService.scanningChange();
        return R.ok();
    }

}
