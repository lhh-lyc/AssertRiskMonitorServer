package com.lhh.serverbase.dto;

import com.lhh.serverbase.entity.HostCompanyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rona
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReScanDto implements Serializable {

    private String uuid;

    private String queueId;

    private String parentDomain;

    private List<HostCompanyEntity> parentDomainList;

    private List<String> hostList;

}
