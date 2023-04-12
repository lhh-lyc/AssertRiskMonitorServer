package com.lhh.serveradmin.service.sys;

import com.lhh.serveradmin.feign.sys.SysDictFeign;
import com.lhh.serveradmin.jwt.config.PassJavaJwtProperties;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.SysDictEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class SysDictService {

    @Autowired
    SysDictFeign sysDictFeign;
    @Resource
    private PassJavaJwtProperties jwtProperties;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;

    public IPage<SysDictEntity> page(Map<String, Object> params) {
        IPage<SysDictEntity> list = sysDictFeign.page(params);
        return list;
    }

    public List<SysDictEntity> list(Map<String, Object> params) {
        List<SysDictEntity> list = sysDictFeign.list(params);
        return list;
    }

    public void save(SysDictEntity dict){
        sysDictFeign.save(dict);
    }

    public void update(SysDictEntity dict){
        sysDictFeign.update(dict);
    }

    public void deleteBatch(Long[] ids){
        sysDictFeign.deleteBatch(ids);
    }

    public SysDictEntity info(Long id){
        return sysDictFeign.info(id);
    }

}
