package com.lhh.serverbase.common.request;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @author Rona
 * @date 2019/4/12 13:13
 */
public class PageUtil {
    public static Page getPageParam(Map<String, Object> params) {
        //分页参数
        Integer currPage = null;
        Integer limit = null;
        if (params.get("page") != null) {
            currPage = Integer.parseInt(MapUtil.getStr(params, "page"));
        }
        if (params.get("limit") != null) {
            limit = Integer.parseInt(MapUtil.getStr(params, "limit"));
        }
        Page page = new Page(currPage, limit);
        if (params.get("sidx") != null) {
            String sidx = String.valueOf(params.get("sidx"));
            if (!StringUtils.isEmpty(sidx)) {
                if (params.get("order") != null) {
                    String order = String.valueOf(params.get("order"));
                    if (StringUtils.isEmpty(order) || order.equals("asc")) {
                        page.setAsc(sidx);
                    } else {
                        page.setDesc(sidx);
                    }
                } else {
                    page.setAsc(sidx);
                }
            }
        }
        page.setOptimizeCountSql(false);
        return page;
    }
}
