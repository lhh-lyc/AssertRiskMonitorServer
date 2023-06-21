package com.lhh.serverReScan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverReScan.dao.ScanHostDao;
import com.lhh.serverReScan.service.ScanHostService;
import com.lhh.serverbase.entity.ScanHostEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("scanHostService")
public class ScanHostServiceImpl extends ServiceImpl<ScanHostDao, ScanHostEntity> implements ScanHostService {

}
