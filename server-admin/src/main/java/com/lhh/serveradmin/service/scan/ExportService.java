package com.lhh.serveradmin.service.scan;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.lhh.serveradmin.feign.scan.ScanHostFeign;
import com.lhh.serveradmin.feign.scan.ScanPortFeign;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.request.IPage;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.entity.ScanHostEntity;
import com.lhh.serverbase.entity.ScanPortEntity;
import com.lhh.serverbase.utils.ImportExcelUtils;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.serverbase.utils.PortUtils;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.serverbase.vo.ScanPortVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired
    ScanHostFeign scanHostFeign;
    @Autowired
    ScanPortFeign scanPortFeign;

    public R upload(MultipartFile file) {
        Map<String, List<List<String>>> excelDataMap = ImportExcelUtils.readExcel(file, 1, 0, 0);
        List<List<String>> dataList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(excelDataMap)) {
            for (String key : excelDataMap.keySet()) {
                dataList = excelDataMap.get(key);
            }
        }
        List<String> domainList = new ArrayList<>();
        List<ScanHostEntity> hostList = new ArrayList<>();
        List<ScanPortEntity> portList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dataList)) {
            for (List<String> data : dataList) {
//                domainList.add(data.get(2));
                if (StringUtils.isEmpty(data.get(4))) {
                    continue;
                }
                ScanHostEntity host = ScanHostEntity.builder()
                        .domain(data.get(2)).parentDomain(data.get(1))
                        .ip(data.get(3)).type(data.get(2).equals(data.get(1)) ? Const.INTEGER_1 : Const.INTEGER_3)
                        .scanPorts(data.get(4)).company(data.get(0))
                        .build();
                hostList.add(host);
                ScanPortEntity port = ScanPortEntity.builder()
                        .ip(data.get(3)).port(Integer.valueOf(data.get(4)))
                        .serverName(data.get(5))
                        .build();
                portList.add(port);
            }
        }
        if (!CollectionUtils.isEmpty(hostList)) {
            scanHostFeign.saveBatch(hostList);
        }
        if (!CollectionUtils.isEmpty(portList)) {
            scanPortFeign.saveBatch(portList);
        }
        /*List<ScanHostEntity> hostList = scanHostFeign.getByDomainList(domainList);
        String parentDomain = "";
        String domain = "";
        String ip = "";
        Map<String, Object> params = new HashMap<>();
        params.put("domain", domain);
        params.put("parentDomain", parentDomain);
        params.put("ip", ip);
        List<ScanHostEntity> hostList = scanHostFeign.equalParams(params);
        if (!CollectionUtils.isEmpty(hostList)) {
            return R.error("");
        }*/
        return R.ok();
    }

    public R upload2(MultipartFile file) {
        Map<String, List<List<String>>> excelDataMap = ImportExcelUtils.readExcel(file, 1, 0, 0);
        List<List<String>> dataList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(excelDataMap)) {
            for (String key : excelDataMap.keySet()) {
                dataList = excelDataMap.get(key);
            }
        }
        List<ScanHostEntity> saveHostList = new ArrayList<>();
        List<ScanHostEntity> updateHostList = new ArrayList<>();
        List<ScanPortEntity> savePortList = new ArrayList<>();
        List<String> domainList = new ArrayList<>();
        List<Long> ipList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dataList)) {
            for (List<String> data : dataList) {
                if (StringUtils.isEmpty(data.get(3)) || StringUtils.isEmpty(data.get(4))) {
                    continue;
                }
                domainList.add(data.get(2));
                ipList.add(IpLongUtils.ipToLong(data.get(3)));
            }
        }

        Map<String, String> parentDomainMap = new HashMap<>();
        List<ScanHostEntity> existDomainList = scanHostFeign.getByDomainList(domainList);
        if (!CollectionUtils.isEmpty(existDomainList)) {
            for (ScanHostEntity domain : existDomainList) {
                parentDomainMap.put(domain.getDomain(), domain.getParentDomain());
            }
        }

        List<ScanHostEntity> existIpList = scanHostFeign.getByIpList(ipList);
        Map<String, List<ScanHostEntity>> ipMap = existIpList.stream().collect(Collectors.groupingBy(h->h.getIp()));

        List<ScanPortEntity> existPortList = scanPortFeign.getByIpList(ipList);
        List<String> ipPortList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(existPortList)) {
            for (ScanPortEntity port : existPortList) {
                ipPortList.add(port.getIp() + Const.STR_CROSSBAR + port.getPort());
            }
        }

        if (!CollectionUtils.isEmpty(dataList)) {
            for (List<String> data : dataList) {
                // scan_port表 ip不存在  或者 ip存在但端口不存在，存一条端口数据
                List<String> portList = PortUtils.getPortList(data.get(4));
                if (CollectionUtils.isEmpty(portList)) {
                    return R.error("端口不能为空！");
                }

                if (!ipMap.containsKey(data.get(3))) {
                    // scan_host表 不包含ip,新增
                    String domain = StringUtils.isEmpty(data.get(2)) ? data.get(3) : data.get(2);
                    // 子域名相同，ip不同，视为错误数据，不保存
                    if (parentDomainMap.containsKey(domain)) {
                        continue;
                    }
                    String parentDomain = Const.STR_EMPTY;
                    if (parentDomainMap.containsKey(domain)) {
                        parentDomain = parentDomainMap.get(domain);
                    } else {
                        parentDomain = StringUtils.isEmpty(data.get(1)) ? StringUtils.isEmpty(data.get(2)) ? data.get(3) : data.get(2) : data.get(1);
                    }
                    Integer isDomain = RexpUtil.isIP(domain) ? Const.INTEGER_0 : Const.INTEGER_1;
                    Integer isMajor = RexpUtil.isMajorDomain(domain) ? Const.INTEGER_1 : Const.INTEGER_0;
                    ScanHostEntity host = ScanHostEntity.builder()
                            .domain(domain)
                            .parentDomain(parentDomain)
                            .ip(data.get(3)).type(data.get(2).equals(data.get(1)) ? Const.INTEGER_1 : Const.INTEGER_3)
                            .scanPorts(data.get(4)).company(data.get(0))
                            .isDomain(isDomain).isMajor(isMajor)
                            .build();
                    saveHostList.add(host);
                } else {
                    List<ScanHostEntity> hostList = ipMap.get(data.get(3));
                    Map<String, ScanHostEntity> hosts = hostList.stream().collect(Collectors.toMap(ScanHostEntity::getDomain, s->s));
                    if (hosts.containsKey(data.get(2))) {
                        // scan_host表 包含ip,同时子域名相同，视为一条存在的数据（不管主域名）
                        ScanHostEntity host = hosts.get(data.get(2));
                        if (!PortUtils.portEquals(host.getScanPorts(), data.get(4))) {
                            host.setScanPorts(PortUtils.getAllPorts(host.getScanPorts(), data.get(4)));
                            updateHostList.add(host);
                        }
                    } else {
                        // scan_host表 包含ip,但子域名不同，视为新数据
                        ScanHostEntity host = ScanHostEntity.builder()
                                .domain(data.get(2)).parentDomain(data.get(1)).ip(data.get(3))
                                .type(Const.INTEGER_3).scanPorts(data.get(4)).company(data.get(0))
                                .build();
                        saveHostList.add(host);
                    }
                }

                for (String p : portList) {
                    if (!ipPortList.contains(data.get(3) + Const.STR_CROSSBAR + p)) {
                        ScanPortEntity port = ScanPortEntity.builder()
                                .ip(data.get(3)).port(Integer.valueOf(p))
                                .serverName(data.get(5))
                                .build();
                        savePortList.add(port);
                        ipPortList.add(data.get(3) + Const.STR_CROSSBAR + p);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(saveHostList)) {
            scanHostFeign.saveBatch(saveHostList);
        }
        if (!CollectionUtils.isEmpty(updateHostList)) {
            for (ScanHostEntity host : updateHostList) {
                scanHostFeign.update(host);
            }
            scanHostFeign.saveBatch(saveHostList);
        }
        if (!CollectionUtils.isEmpty(savePortList)) {
            scanPortFeign.saveBatch(savePortList);
        }
        return R.ok();
    }

    public List<ScanPortVo> exportPorts(Map<String, Object> params) {
        List<ScanPortVo> list = scanPortFeign.exportList(params);
        return list;
    }

}
