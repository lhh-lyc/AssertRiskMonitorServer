package com.lhh.serverReScan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverReScan.dao.ScanProjectHostDao;
import com.lhh.serverReScan.service.ScanProjectHostService;
import com.lhh.serverbase.entity.ScanProjectHostEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("scanProjectHostService")
public class ScanProjectHostServiceImpl extends ServiceImpl<ScanProjectHostDao, ScanProjectHostEntity> implements ScanProjectHostService {

    @Autowired
    private ScanProjectHostDao scanProjectHostDao;

}
