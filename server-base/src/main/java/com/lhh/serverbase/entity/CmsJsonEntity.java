package com.lhh.serverbase.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;

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
}
