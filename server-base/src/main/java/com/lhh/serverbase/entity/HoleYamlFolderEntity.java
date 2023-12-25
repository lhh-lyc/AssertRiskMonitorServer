package com.lhh.serverbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
@TableName("hole_yaml_folder")
public class HoleYamlFolderEntity extends BaseEntity {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * ID
     */
    private Long parentId;
    /**
     *
     */
    private String label;
    /**
     *
     */
    private String folder;
    /**
     *
     */
    private String ancestors;

    private String parentName;

    @TableField(exist = false)
    private List<HoleYamlFolderEntity> children;

}
