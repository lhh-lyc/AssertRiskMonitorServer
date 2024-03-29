package com.lhh.serveradmin.service.sys;

import cn.hutool.core.map.MapUtil;
import com.lhh.serveradmin.feign.sys.SysUserFeign;
import com.lhh.serveradmin.feign.sys.SysUserRoleFeign;
import com.lhh.serveradmin.jwt.common.ResponseCodeEnum;
import com.lhh.serveradmin.jwt.common.ResponseResult;
import com.lhh.serveradmin.jwt.config.PassJavaJwtProperties;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.entity.SysUserEntity;
import com.lhh.serverbase.entity.SysUserRoleEntity;
import com.lhh.serverbase.utils.MD5;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysUserService {

    @Autowired
    SysUserFeign sysUserFeign;
    @Autowired
    SysUserRoleFeign sysUserRoleFeign;
    @Resource
    private PassJavaJwtProperties jwtProperties;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;

    public IPage<SysUserEntity> page(Map<String, Object> params) {
        IPage<SysUserEntity> list = sysUserFeign.page(params);
        return list;
    }

    public List<SysUserEntity> list(Map<String, Object> params) {
        List<SysUserEntity> list = sysUserFeign.list(params);
        return list;
    }

    public ResponseResult<?> login(@RequestBody Map<String, Object> params) {
        // 从请求体中获取用户名密码
        String userName = MapUtil.getStr(params, jwtProperties.getUserParamName());
        String password = MapUtil.getStr(params, jwtProperties.getPwdParamName());

        // 如果用户名和密码为空
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)) {
            return ResponseResult.error(ResponseCodeEnum.LOGIN_ERROR.getCode(), ResponseCodeEnum.LOGIN_ERROR.getMessage());
        }
        // 根据 userId 去数据库查找该用户
        SysUserEntity sysUser = sysUserFeign.queryByName(userName);
        if (sysUser != null) {
            password = MD5.getEncryptPwd(password, sysUser.getSalt());
            // 将数据库的加密密码与用户明文密码做比对
//            boolean isAuthenticated = passwordEncoder.matches(password, sysUser.getPassword());
            // 如果密码匹配成功
            if (sysUser.getPassword().equals(password)) {
                // 通过 jwtTokenUtil 生成 JWT 令牌和刷新令牌
                Map<String, Object> tokenMap = jwtTokenUtil.generateTokenAndRefreshToken(String.valueOf(sysUser.getUserId()), userName);
                tokenMap.put("userId", sysUser.getUserId());
                tokenMap.put("userName", sysUser.getUserName());
                tokenMap.put("encUserId", MD5.getEncryptPwd(sysUser.getUserId().toString(), Const.STR_SALT));
                tokenMap.put("isAdmin", isAdmin(sysUser.getUserId()));
                return ResponseResult.success(tokenMap);
            }
            // 如果密码匹配失败
            return ResponseResult.error(ResponseCodeEnum.LOGIN_ERROR.getCode(), ResponseCodeEnum.LOGIN_ERROR.getMessage());
        }
        // 如果未找到用户
        return ResponseResult.error(ResponseCodeEnum.LOGIN_ERROR.getCode(), ResponseCodeEnum.LOGIN_ERROR.getMessage());
    }

    public SysUserEntity info(Long userId) {
        SysUserEntity user = sysUserFeign.info(userId);
        List<SysUserRoleEntity> roleList = sysUserRoleFeign.list(new HashMap<String, Object>(){{put("userId", userId);}});
        List<Long> roleIdList = roleList.stream().map(SysUserRoleEntity::getRoleId).collect(Collectors.toList());
        user.setRoleIdList(roleIdList);
        return user;
    }

    public void save(SysUserEntity user) {
        String salt = RandomStringUtils.randomAlphanumeric(20);
        user.setSalt(salt);
        String pwd = MD5.encryptPwdFirst("123456");
        String password = MD5.getEncryptPwd(pwd, salt);
        user.setPassword(password);
        Long userId = sysUserFeign.save(user);
        List<SysUserRoleEntity> sysUserRoleList = new ArrayList<>();
        for (Long roleId : user.getRoleIdList()) {
            SysUserRoleEntity sysUserRoleEntity = SysUserRoleEntity.builder().userId(userId).roleId(roleId).build();
            sysUserRoleList.add(sysUserRoleEntity);
        }
        sysUserRoleFeign.saveBatch(sysUserRoleList);
    }

    public void update(SysUserEntity user) {
        sysUserFeign.update(user);
    }

    public void deleteBatch(Long[] ids){
        sysUserFeign.deleteBatch(ids);
    }

    public Integer isAdmin(Long userId){
        List<Long> adminIdList = sysUserFeign.getAdminIdList();
        Integer flag = adminIdList.contains(userId) ? Const.INTEGER_1 : Const.INTEGER_0;
        return flag;
    }

    public void resetPwd(List<Long> ids){
        Map<String, Object> params = new HashMap<>();
        params.put("userIdList", ids);
        List<SysUserEntity> userList = sysUserFeign.list(params);
        if (!CollectionUtils.isEmpty(userList)) {
            for (SysUserEntity user : userList) {
                String pwd = MD5.encryptPwdFirst("123456");
                String password = MD5.getEncryptPwd(pwd, user.getSalt());
                user.setPassword(password);
                sysUserFeign.update(user);
            }
        }
    }

}
