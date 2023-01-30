package com.lhh.serverbase.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 企业_基础信息表
 *
 * @author lyc
 * @date 2019-04-18 16:57:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {

    /**
     * 标记(0:正常,1:逻辑删除)
     * TableLogic 逻辑删除
     */
    private Integer delFlg = 0;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 更新时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
     * 创建人id
     */
    private Long createId;
    /**
     * 更新人id
     */
    private Long updateId;

}
