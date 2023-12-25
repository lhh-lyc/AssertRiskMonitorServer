package com.lhh.serverinfocommon.dao.scan;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lhh.serverbase.entity.HoleYamlFolderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统_文件表
 *
 * @author lyc
 * @date 2022-12-28 14:21:23
 */
@Mapper
public interface HoleYamlFolderDao extends BaseMapper<HoleYamlFolderEntity> {

    /**
     * 分页查询用户列表
     * @param params
     * @return
     */
    List<HoleYamlFolderEntity> queryList(Map<String, Object> params);

}
