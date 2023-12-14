package com.lhh.serveradmin.service.scan;

import com.lhh.serveradmin.feign.scan.HoleYamlFeign;
import com.lhh.serveradmin.feign.scan.ScanSecurityHoleFeign;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.HoleYamlEntity;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


/**
 * 系统_用户表应用服务层
 *
 * @author lyc
 * @date 2023-09-12 15:41:27
 */
@Service
public class HoleYamlService {

    @Autowired
    private HoleYamlFeign holeYamlFeign;

    /**
     * 查询 分页数据
     *
     * @param
     */
    public R page(Map<String, Object> params) {
        IPage<HoleYamlEntity> page = holeYamlFeign.page(params);
        return R.ok(page);
    }

    /**
     * 查询 列表数据
     *
     * @param
     */
    public R list(Map<String, Object> params) {
        List<HoleYamlEntity> list = holeYamlFeign.list(params);
        return R.ok(list);
    }

}



