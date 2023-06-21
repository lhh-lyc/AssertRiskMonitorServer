package com.lhh.serverTask.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverTask.dao.ScanHostDao;
import com.lhh.serverTask.service.ScanHostService;
import com.lhh.serverbase.entity.ScanHostEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("scanHostService")
public class ScanHostServiceImpl extends ServiceImpl<ScanHostDao, ScanHostEntity> implements ScanHostService {

    @Autowired
    ScanHostDao scanHostDao;

    @Override
    public List<String> getParentList() {
        return scanHostDao.getParentList();
    }
}
