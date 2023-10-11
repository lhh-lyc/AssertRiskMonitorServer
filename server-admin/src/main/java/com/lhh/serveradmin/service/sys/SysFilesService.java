package com.lhh.serveradmin.service.sys;

import com.lhh.serveradmin.feign.sys.SysFilesFeign;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.SysFilesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SysFilesService {

    @Autowired
    SysFilesFeign sysFilesFeign;

    public IPage<SysFilesEntity> page(Map<String, Object> params) {
        IPage<SysFilesEntity> list = sysFilesFeign.page(params);
        return list;
    }

}
