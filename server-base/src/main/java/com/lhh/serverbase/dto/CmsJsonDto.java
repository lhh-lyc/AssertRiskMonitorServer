package com.lhh.serverbase.dto;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmsJsonDto {

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
    private List<String> keyword;

}
