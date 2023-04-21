package com.lhh.serverbase.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Insert;

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
    @ExcelIgnore
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlg = 0;
    /**
     * 创建时间
     */
    @ExcelIgnore
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 更新时间
     */
    @ExcelIgnore
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
     * 创建人id
     */
    @ExcelIgnore
    private Long createId;
    /**
     * 更新人id
     */
    @ExcelIgnore
    private Long updateId;

}
