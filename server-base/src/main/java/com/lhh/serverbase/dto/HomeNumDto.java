package com.lhh.serverbase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeNumDto {

    private Integer companyNum;

    private Integer primaryDomainNum;

    private Integer subDomainNum;

    private Integer ipNum;

    private Integer portNum;

}
