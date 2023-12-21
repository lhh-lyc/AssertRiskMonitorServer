package com.lhh.serveradmin.controller.sys;

import cn.hutool.core.map.MapUtil;
import com.lhh.serveradmin.service.sys.SysDictService;
import com.lhh.serveradmin.service.sys.SysSettingService;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.SysDictEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("sys/setting")
public class SysSettingController {

    @Autowired
    SysSettingService sysSettingService;

    /**
     * 保存
     *
     * @return
     */
    @PostMapping("/saveServer")
    public R saveServer(@RequestBody Map<String, Object> params) {
        try {
            String serverNames = MapUtil.getStr(params, "serverNames");
            if (!StringUtils.isEmpty(serverNames)) {
                sysSettingService.saveServer(serverNames);
            }
        } catch (Exception e) {
            R.failed("新增失败！");
        }
        return R.ok();
    }

    @RequestMapping("/server/info")
    public R serverInfo() {
        return R.ok().put("data", sysSettingService.serverInfo());
    }

}
