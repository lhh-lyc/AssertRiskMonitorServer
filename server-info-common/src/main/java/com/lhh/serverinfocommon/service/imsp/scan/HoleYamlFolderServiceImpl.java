package com.lhh.serverinfocommon.service.imsp.scan;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhh.serverbase.entity.HoleYamlFolderEntity;
import com.lhh.serverinfocommon.dao.scan.HoleYamlFolderDao;
import com.lhh.serverinfocommon.service.scan.HoleYamlFolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("HoleYamlFolderService")
public class HoleYamlFolderServiceImpl extends ServiceImpl<HoleYamlFolderDao, HoleYamlFolderEntity> implements HoleYamlFolderService {

    @Autowired
    private HoleYamlFolderDao holeYamlFolderDao;

    /**
     * 查询列表数据
     * @param params
     * @return
     */
    @Override
    public List<HoleYamlFolderEntity> list(Map<String, Object> params) {
        return holeYamlFolderDao.queryList(params);
    }

}
