package com.lhh.serverTask.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverTask.dao.ScanPortDao;
import com.lhh.serverTask.service.ScanPortService;
import com.lhh.serverbase.entity.ScanPortEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("scanPortService")
public class ScanPortServiceImpl extends ServiceImpl<ScanPortDao, ScanPortEntity> implements ScanPortService {

    @Autowired
    private ScanPortDao scanPortDao;

    @Override
    public List<ScanPortEntity> basicByIpList(List<Long> ipList) {
        return scanPortDao.basicByIpList(ipList);
    }

    @Override
    public void deleteByIp(Long ip) {
        scanPortDao.deleteByIp(ip);
    }

    @Override
    public void deleteBatch(List<Long> idList) {
        scanPortDao.deleteBatch(idList);
    }

    @Override
    public List<Integer> queryDomainPortList(String domain) {
        return scanPortDao.queryDomainPortList(domain);
    }

}
