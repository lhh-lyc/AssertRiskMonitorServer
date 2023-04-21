package com.lhh.serverbase.dto;

import com.lhh.serverbase.entity.SysMenuEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Rona
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupTagDto {

    private String tagName;

    private String tagValue;

}
