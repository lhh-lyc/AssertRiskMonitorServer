package com.lhh.serverTask.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverTask.dao.ScanProjectHostDao;
import com.lhh.serverTask.service.ScanProjectHostService;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("scanProjectHostService")
public class ScanProjectHostServiceImpl extends ServiceImpl<ScanProjectHostDao, ScanProjectHostEntity> implements ScanProjectHostService {

    @Autowired
    private ScanProjectHostDao scanProjectHostDao;

}
