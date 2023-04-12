package com.lhh.serveradmin.service.sys;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lhh.serveradmin.feign.sys.SysMenuFeign;
import com.lhh.serverbase.dto.MenuDto;
import com.lhh.serverbase.entity.SysMenuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysMenuService {

    @Autowired
    SysMenuFeign sysMenuFeign;

    public List<SysMenuEntity> list(Map<String, Object> params){
        List<SysMenuEntity> list = sysMenuFeign.list(params);
        return list;
    }

    public MenuDto nav(Long userId){
        return sysMenuFeign.nav(userId);
    }

    public void save(SysMenuEntity menu){
        sysMenuFeign.save(menu);
    }

    public void update(SysMenuEntity menu){
        sysMenuFeign.update(menu);
    }

    public void deleteBatch(Long[] ids){
        sysMenuFeign.deleteBatch(ids);
    }

    public SysMenuEntity info(Long id){
        return sysMenuFeign.info(id);
    }

    public List<SysMenuEntity> findAll(){
        return sysMenuFeign.findAll();
    }

    /**
     * 获取当前节点下所有子节点包括自身
     *
     * @param parentId
     * @return
     */
    public Set<Long> getChild(Long parentId) {
        Set<Long> list = sysMenuFeign.getChild(parentId);
        return list;
    }

}
