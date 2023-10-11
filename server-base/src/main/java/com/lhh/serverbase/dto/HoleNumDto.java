package com.lhh.serverbase.dto;

import com.lhh.serverbase.entity.SysMenuEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Rona
 * 菜单权限Dto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoleNumDto {

    private Long projectId;

    private Integer mediumNum;

    private Integer highNum;

    private Integer criticalNum;

}
