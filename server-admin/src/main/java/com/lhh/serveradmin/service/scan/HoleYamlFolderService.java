package com.lhh.serveradmin.service.scan;

import com.lhh.serveradmin.feign.scan.HoleYamlFolderFeign;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.HoleYamlFolderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 系统_用户表应用服务层
 *
 * @author lyc
 * @date 2023-09-12 15:41:27
 */
@Slf4j
@Service
public class HoleYamlFolderService {

    @Autowired
    private HoleYamlFolderFeign holeYamlFolderFeign;

    /**
     * 查询 列表数据
     *
     * @param
     */
    public R list(Map<String, Object> params) {
        List<HoleYamlFolderEntity> list = holeYamlFolderFeign.list(params);
        Map<Long, HoleYamlFolderEntity> idNodeMap = new HashMap<>();
        List<HoleYamlFolderEntity> topLevelNodes = new ArrayList<>();

        // 构建 id 和节点的映射关系，同时找出所有顶级节点
        for (HoleYamlFolderEntity node : list) {
            idNodeMap.put(node.getId(), node);
            if (Const.LONG_0.equals(node.getParentId())) {
                topLevelNodes.add(node);
            }
        }

        // 根据父子关系将子节点添加到父节点的 children 列表中
        for (HoleYamlFolderEntity node : list) {
            Long parentId = node.getParentId();
            if (parentId != null) {
                HoleYamlFolderEntity parentNode = idNodeMap.get(parentId);
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                }
            }
        }
        return R.ok(topLevelNodes);
    }

}



