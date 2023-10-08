package com.lhh.serveradmin.service.scan;

import com.lhh.serveradmin.feign.scan.ScanHostFeign;
import com.lhh.serveradmin.feign.scan.ScanPortFeign;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanPortEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ScanPortService {

    @Autowired
    ScanPortFeign scanPortFeign;
    @Autowired
    ScanHostFeign scanHostFeign;

    public IPage<ScanPortEntity> page(Map<String, Object> params) {
        IPage<ScanPortEntity> list = scanPortFeign.page(params);
        return list;
    }

    public IPage<ScanHostEntity> queryPage(Map<String, Object> params) {
        IPage<ScanHostEntity> list = scanHostFeign.page(params);
        return list;
    }

    public void delete(Map<String, Object> params) {
        scanPortFeign.deleteByIpPort(params);
    }

    public void deleteByTag(Map<String, Object> params) {
        scanPortFeign.deleteByTag(params);
    }

}
