package com.lhh.serveradmin.service.sys;

import com.lhh.serveradmin.utils.JedisUtils;
import com.lhh.serverbase.common.constant.CacheConst;
import com.lhh.serverbase.common.constant.Const;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SysSettingService {

    public void saveServer(String data){
        List<String> serverList = new ArrayList<>(Arrays.asList(data.replace(" ", "").split(Const.STR_LINEFEED)));
        JedisUtils.setJson(CacheConst.REDIS_SERVER_NAMES, String.join(Const.STR_COMMA, serverList));
    }

    public String serverInfo(){
        return JedisUtils.getJson(CacheConst.REDIS_SERVER_NAMES);
    }

}
