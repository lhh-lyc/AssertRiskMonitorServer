package com.lhh.serverbase.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Rona
 * @date 2019/4/18 13:08
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IPage<T> {

    private List<T> records;

    private Long total;

    private Long size;

    private Long current;

    private Long pages;

    private String ascs;

    private String descs;

    private boolean optimizeCountSql;

    private boolean isSearchCount;


}
