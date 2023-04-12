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
public class MenuDto {

    /**
     * 菜单列表
     */
    private List<SysMenuEntity> menuList;

    /**
     * 权限列表
     */
    private List<String> permissions;

}
