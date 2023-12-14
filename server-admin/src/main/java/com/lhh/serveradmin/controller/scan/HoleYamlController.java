package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.service.scan.HoleYamlService;
import com.lhh.serveradmin.service.scan.ScanProjectService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanProjectEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("hole/yaml")
public class HoleYamlController {

    @Autowired
    HoleYamlService holeYamlService;

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(holeYamlService.page(params));
    }

}
