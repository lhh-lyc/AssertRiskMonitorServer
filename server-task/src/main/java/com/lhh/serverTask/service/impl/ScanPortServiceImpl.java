package com.lhh.serverTask.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverTask.dao.ScanPortDao;
import com.lhh.serverTask.service.ScanPortService;
import com.lhh.serverbase.entity.ScanPortEntity;
import org.springframework.stereotype.Service;


@Service("scanPortService")
public class ScanPortServiceImpl extends ServiceImpl<ScanPortDao, ScanPortEntity> implements ScanPortService {

}
