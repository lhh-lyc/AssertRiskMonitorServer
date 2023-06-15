package com.lhh.serverbase.dto;

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
public class ReScanDto {

    private String queueId;

    private List<String> hostList;

}
