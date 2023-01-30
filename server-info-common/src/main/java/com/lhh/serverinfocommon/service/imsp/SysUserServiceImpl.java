package com.lhh.serverinfocommon.service.imsp;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.SysUserEntity;
import com.lhh.serverbase.utils.Query;
import com.lhh.serverinfocommon.dao.SysUserDao;
import com.lhh.serverinfocommon.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


@Service("sysUserService")
public class SysUserServiceImpl extends ServiceImpl<SysUserDao, SysUserEntity> implements SysUserService {

    /**
     * 分页查询列表数据
     * @param params
     * @return
     */
    @Override
    public IPage<SysUserEntity> page(Map<String, Object> params) {
        Long userId = MapUtil.getLong(params,  "userId");
        IPage<SysUserEntity> page = this.page(
                new Query<SysUserEntity>().getPage(params),
                new QueryWrapper<SysUserEntity>()
                        .eq(userId != null, "user_id", userId)
        );
        return page;
    }

    /**
     * 查询用户列表
     * @param params
     * @return
     */
    @Override
    public List<SysUserEntity> list(Map<String, Object> params) {
        QueryWrapper wrapper = Wrappers.query();
        String userName = MapUtil.getStr(params, "userName");
        wrapper.eq(!StringUtils.isEmpty(userName), "user_name", userName);
        List<SysUserEntity> list = list(wrapper);
        return list;
    }

    @Override
    public SysUserEntity queryByName(String userName) {
        QueryWrapper wrapper = Wrappers.query()
                .eq(!StringUtils.isEmpty(userName), "user_name", userName);
        List<SysUserEntity> list = list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

}
