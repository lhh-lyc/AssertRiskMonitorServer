package com.lhh.serveradmin.service.scan;

import com.lhh.serveradmin.feign.scan.ScanHostFeign;
import com.lhh.serveradmin.feign.sys.SysDictFeign;
import com.lhh.serveradmin.jwt.config.PassJavaJwtProperties;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.dto.ScanResultDto;
import com.lhh.serverbase.entity.SysDictEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class ScanHostService {

    @Autowired
    ScanHostFeign scanHostFeign;

    public List<ScanResultDto> list(Map<String, Object> params) {
        List<ScanResultDto> list = scanHostFeign.getDomainGroupList(params);
        return list;
    }

}
