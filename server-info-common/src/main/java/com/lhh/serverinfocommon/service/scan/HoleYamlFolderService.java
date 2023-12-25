package com.lhh.serverinfocommon.service.scan;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lhh.serverbase.entity.HoleYamlFolderEntity;

import java.util.List;
import java.util.Map;

/**
 * 系统_文件表服务层
 *
 * @author lyc
 * @email lyc@gmail.com
 * @date 2022-12-28 14:21:23
 */
public interface HoleYamlFolderService extends IService<HoleYamlFolderEntity> {

    /**
    * 根据参数查询列表
    * @param params
    * @return
    */
    List<HoleYamlFolderEntity> list(Map<String, Object> params);

}

