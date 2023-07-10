package com.lhh.serverTask.controller;

import com.lhh.serverTask.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TaskController {

    @Autowired
    TaskService taskService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("weekTask")
    public void weekTask(){
        taskService.weekTask();
    }

}
