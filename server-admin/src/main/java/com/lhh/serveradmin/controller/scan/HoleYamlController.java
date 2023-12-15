package com.lhh.serveradmin.controller.scan;

import com.lhh.serveradmin.service.scan.HoleYamlService;
import com.lhh.serverbase.common.response.R;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("hole/yaml")
public class HoleYamlController {

    @Autowired
    HoleYamlService holeYamlService;

    @GetMapping("page")
    public R page(@RequestParam Map<String, Object> params){
        return R.ok(holeYamlService.page(params));
    }

    @PostMapping("downYaml")
    public R downYaml(@RequestBody List<String> fileNameList){
        return holeYamlService.downYaml(fileNameList);
    }

    /**
     * 单个删除
     * @param ids
     * @return
     */
    @PostMapping("delete")
    public R delete(@RequestBody List<Long> ids) {
        return holeYamlService.delete(ids);
    }

}
