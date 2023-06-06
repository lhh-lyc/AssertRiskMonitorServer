package com.lhh.serveradmin.controller.sys;

import cn.hutool.core.map.MapUtil;
import com.lhh.serveradmin.jwt.common.ResponseResult;
import com.lhh.serveradmin.jwt.utils.PassJavaJwtTokenUtil;
import com.lhh.serveradmin.service.sys.SysUserService;
import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.common.constant.TokenConstants;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.SysUserEntity;
import com.lhh.serverbase.utils.MD5;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("auth")
public class LoginController {

    @Autowired
    SysUserService sysUserService;
    @Resource
    private PassJavaJwtTokenUtil jwtTokenUtil;

    /**
     * 使用用户名密码换 JWT 令牌
     */
    @PostMapping("/login")
    public ResponseResult<?> login(@RequestBody Map<String,Object> params){
        return sysUserService.login(params);
    }

    /**
     * 系统用户退出
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/logout")
    public R logOut(HttpServletRequest request) {
        String token = request.getHeader(TokenConstants.AUTHENTICATION);

        if (StringUtils.isEmpty(token)) {
            return R.ok();
        }
        String userId = jwtTokenUtil.getUserIdFromToken(token);
        jwtTokenUtil.delCacheToken(userId);
        return R.ok();
    }

    /**
     * 修改登录用户密码
     */
    @PostMapping("/updatePwd")
    public R password(@RequestBody Map<String, Object> params) {
        //用户信息
        Long userId = MapUtil.getLong(params, "userId");
        String password = MapUtil.getStr(params, "password");
        String newPassword = MapUtil.getStr(params, "newPassword");
        SysUserEntity user = sysUserService.info(userId);
        String tmpPwd = MD5.getEncryptPwd(password, user.getSalt());
        // 旧密码错误
        if (user == null || !user.getPassword().equals(tmpPwd)) {
            return R.failed("原密码错误！");
        }

        // 新密码不能与旧密码相同
        if (password.equals(newPassword)) {
            return R.failed("新密码不能与旧密码相同");
        }
        //新密码
        newPassword = MD5.getEncryptPwd(newPassword, user.getSalt());
        SysUserEntity userEntity = new SysUserEntity();
        userEntity.setUpdateTime(new Date());
        userEntity.setPassword(newPassword);
        userEntity.setUserId(userId);
        userEntity.setUpdateId(userId);
        //更新密码
        try {
            sysUserService.update(userEntity);
        } catch (Exception e) {
            R.failed("更新失败！");
        }
        return R.ok();
    }

}
