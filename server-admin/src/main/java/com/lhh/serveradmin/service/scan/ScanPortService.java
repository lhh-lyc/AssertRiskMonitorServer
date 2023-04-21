package com.lhh.serveradmin.service.scan;

import com.lhh.serveradmin.feign.scan.ScanPortFeign;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.ScanPortEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScanPortService {

    @Autowired
    ScanPortFeign scanPortFeign;

    public IPage<ScanPortEntity> page(Map<String, Object> params) {
        IPage<ScanPortEntity> list = scanPortFeign.page(params);
        return list;
    }

    public void delete(Map<String, Object> params) {
        scanPortFeign.deleteByIpPort(params);
    }

}
