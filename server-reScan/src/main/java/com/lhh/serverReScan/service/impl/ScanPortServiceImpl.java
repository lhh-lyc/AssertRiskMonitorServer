package com.lhh.serverReScan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverReScan.dao.ScanPortDao;
import com.lhh.serverReScan.service.ScanPortService;
import com.lhh.serverbase.entity.ScanPortEntity;
import org.springframework.stereotype.Service;


@Service("scanPortService")
public class ScanPortServiceImpl extends ServiceImpl<ScanPortDao, ScanPortEntity> implements ScanPortService {

}
