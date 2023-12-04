package com.lhh.serverbase.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lhh.serverbase.dto.CmsJsonDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 系统_用户表
 *
 * @author lyc
 * @date 2023-07-18 15:08:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("cms_json")
public class CmsJsonEntity {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     *
     */
    private String cms;
    /**
     *
     */
    private String method;
    /**
     *
     */
    private String location;
    /**
     *
     */
    private String keyword;
    /**
     *
     */
    @TableField(exist = false)
    private List<String> keywordList;

    public void addKeywords(List<String> keywords) {
        if (this.keywordList == null) {
            this.keywordList = new ArrayList<>();
        }
        this.keywordList.addAll(keywords);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (StringUtils.isEmpty(cms) ? 0 : cms.hashCode());
        result = prime * result + (method == null ? 0 : method.hashCode());
        result = prime * result + (StringUtils.isEmpty(location)? 0 : location.hashCode());
        result = prime * result + (StringUtils.isEmpty(keyword) ? 0 : keyword.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CmsJsonEntity json = (CmsJsonEntity) obj;
        if (StringUtils.isEmpty(cms)) {
            if (!StringUtils.isEmpty(json.cms)) {
                return false;
            }
        } else if (!cms.equals(json.cms)) {
            return false;
        }
        if (method == null) {
            if (json.method != null) {
                return false;
            }
        } else if (!method.equals(json.method)) {
            return false;
        }
        if (StringUtils.isEmpty(location)) {
            if (!StringUtils.isEmpty(json.location)) {
                return false;
            }
        } else if (!location.equals(json.location)) {
            return false;
        }
        if (StringUtils.isEmpty(keyword)) {
            if (!StringUtils.isEmpty(json.keyword)) {
                return false;
            }
        } else if (!keyword.equals(json.keyword)) {
            return false;
        }
        return true;
    }

}
