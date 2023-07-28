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
public class CompanyTask {

    @Autowired
    TaskService taskService;

    @Scheduled(cron = "0 0 * * * ? ")
    @GetMapping("companyTask")
    public void companyTask(){
        taskService.companyTask();
    }

}
