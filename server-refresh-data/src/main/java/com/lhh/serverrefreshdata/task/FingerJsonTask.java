package com.lhh.serverrefreshdata.task;

import com.lhh.serverrefreshdata.service.TaskService;
import com.lhh.serverbase.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class FingerJsonTask {

    @Autowired
    TaskService taskService;

    /**
     * 获取finger匹配的favicon hash值
     * @return
     */
//    @Scheduled(cron = "0 * * * * ? ")
    @GetMapping("fingerJson")
    public R fingerJson() {
        log.info("FingerJson定时任务开始");
        taskService.fingerJson();
        return R.ok();
    }

}
