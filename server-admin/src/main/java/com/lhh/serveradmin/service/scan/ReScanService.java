package com.lhh.serveradmin.service.scan;

import cn.hutool.core.map.MapUtil;
import com.lhh.serveradmin.feign.scan.ScanHostFeign;
import com.lhh.serveradmin.mqtt.ReScanSender;
import com.lhh.serverbase.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReScanService {

    @Autowired
    ScanHostFeign scanHostFeign;
    @Autowired
    ReScanSender reScanSender;

    public R rescan(Map<String, Object> params) {
        Integer tagType = MapUtil.getInt(params, "tagType");
        List<String> selectList = (List<String>) params.get("selectList");
        Map<String, Object> filter = new HashMap<>();
        switch (tagType) {
            case 2:
                filter.put("companyList", selectList);
                break;
            case 3:
                filter.put("parentDomainList", selectList);
                break;
            case 4:
                filter.put("domainList", selectList);
                break;
        }
        List<String> list = scanHostFeign.getParentDomainList(filter);
        reScanSender.reScanDomainToMqtt(list);
        return R.ok();
    }

}
