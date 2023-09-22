package com.lhh.servermonitor.controller;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.response.R;
import com.lhh.serverbase.dto.ScanParamDto;
import com.lhh.serverbase.entity.ScanHostPortEntity;
import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.utils.IpLongUtils;
import com.lhh.servermonitor.service.ScanHoleService;
import com.lhh.servermonitor.service.ScanService;
import com.lhh.servermonitor.service.TmpRedisService;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.servermonitor.utils.HttpxCustomizeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("")
@Slf4j
public class test {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    TmpRedisService tmpRedisService;
    @Autowired
    ScanService scanService;
    @Autowired
    ScanHoleService scanHoleService;
    @Value("${dir-setting.tool-dir}")
    private String toolDir;

    @GetMapping("test")
    public R test(String url){
        Map<String, String> result = new HashMap<>();
        try {
            result = HttpxCustomizeUtils.getUrlMap(stringRedisTemplate, toolDir, url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(JSON.toJSONString(result));
        return R.ok().put("data", result);
    }

    @GetMapping("dealList")
    public R dealList(String domain){
        String s = "yohei.preipo.org.cn,lisa.preipo.org.cn,taiwa.preipo.org.cn,d236.preipo.org.cn,hls.preipo.org.cn,databases.preipo.org.cn,mutti.preipo.org.cn,backend.athanasius.preipo.org.cn,html-pedia.preipo.org.cn,light.preipo.org.cn,zmu.preipo.org.cn,unze.preipo.org.cn,ppp94.preipo.org.cn,netzhansa.preipo.org.cn,wxg.preipo.org.cn,cevax.preipo.org.cn,yly.preipo.org.cn,attach.aegis-lq.preipo.org.cn,push.res.preipo.org.cn,musicians.preipo.org.cn,cjnglfcjmoppphdb.cbnecjecbimgdjmo0.fs.preipo.org.cn,paspartoy.preipo.org.cn,frz40.preipo.org.cn,ppnx.preipo.org.cn,key.preipo.org.cn,lynx.preipo.org.cn,328.o.preipo.org.cn,ars.conecta.preipo.org.cn,serenity.preipo.org.cn,uro.preipo.org.cn,ulea.preipo.org.cn,regis.preipo.org.cn,lee.preipo.org.cn,adsl.surveys.preipo.org.cn,rzj.preipo.org.cn,ketl.preipo.org.cn,kactoos.preipo.org.cn,yjq.preipo.org.cn,88.barcelona.preipo.org.cn,maher.preipo.org.cn,gno.preipo.org.cn,style.preipo.org.cn,phyk.preipo.org.cn,hungdudes.preipo.org.cn,nbms.preipo.org.cn,icfun.preipo.org.cn,cd.preipo.org.cn,zoho.preipo.org.cn,imgdbaienfkfkfcb1.61.alarmhub-callback-mngr.preipo.org.cn,ppp95-140-129-61.preipo.org.cn,abragam.a009.preipo.org.cn,ebsco.preipo.org.cn,pc59.preipo.org.cn,hur.preipo.org.cn,xmgv.preipo.org.cn,www.c.preipo.org.cn,german.preipo.org.cn,vys.preipo.org.cn,nsg.preipo.org.cn,mediaonline-dhetemplate.preipo.org.cn,lotus.preipo.org.cn,b5.caromac.preipo.org.cn,snsadmin.n.preipo.org.cn,ino.preipo.org.cn,strategie.preipo.org.cn,zwk.preipo.org.cn,nesse.preipo.org.cn,mistress-a.preipo.org.cn,beau.webmail.preipo.org.cn,platinum.preipo.org.cn,dyn41.preipo.org.cn,aad.contract-exp.preipo.org.cn,rustoleum.preipo.org.cn,blade.asok.preipo.org.cn,vses.preipo.org.cn,yjt.preipo.org.cn,glnglnndjmgdbiek.bnaaiecjeknopphd0.athletics.376.preipo.org.cn,csair.preipo.org.cn,par2.preipo.org.cn,forties.preipo.org.cn,pmgt.preipo.org.cn,wiki.preipo.org.cn,magazine.preipo.org.cn,ynml.preipo.org.cn,hyg.preipo.org.cn,jxzb.preipo.org.cn,hhd.preipo.org.cn,114.preipo.org.cn,ciris.preipo.org.cn,ppccg.preipo.org.cn,360.xinzhou.preipo.org.cn,sdec.preipo.org.cn,bbg.preipo.org.cn,dowwnserv.preipo.org.cn,ruohikolla.preipo.org.cn,kombu.preipo.org.cn,tphil.preipo.org.cn,stats.fun.preipo.org.cn,kawa.preipo.org.cn,kfp.preipo.org.cn,tuwen.preipo.org.cn,moby.preipo.org.cn,172.anestintherocks.preipo.org.cn,faces.preipo.org.cn,kemi.preipo.org.cn,xkq.preipo.org.cn,snapshots.preipo.org.cn,otd.preipo.org.cn,wkb.preipo.org.cn,ppp190-146-129-61.preipo.org.cn,04g5o7h1cweoyxvn.1737qipaiyouxizhongxin.preipo.org.cn,basse.preipo.org.cn,2015.beckie.preipo.org.cn,kei.preipo.org.cn,pme.preipo.org.cn,dns03.preipo.org.cn,b6.bet365tiyupuke.preipo.org.cn,salsa.preipo.org.cn,nick.preipo.org.cn,yiliao.preipo.org.cn,me4.m.preipo.org.cn,aap.web.preipo.org.cn,moppppphlfcjncje.315.akakdisebalikpintu.preipo.org.cn,jimk.preipo.org.cn,s171.preipo.org.cn,nintendoswitch.preipo.org.cn,s56.preipo.org.cn,ppp189-29-109-202.preipo.org.cn,d82.preipo.org.cn,pipit.preipo.org.cn,oort-pusher.preipo.org.cn,bars.preipo.org.cn";
        List<String> subList = new ArrayList<>(Arrays.asList(s.split(Const.STR_COMMA)));
        List<ScanParamDto> list = scanService.dealList(1L, "1-65535", domain, subList, new ArrayList<>());
        return R.ok(list);
    }

    @GetMapping("getSubDomainTest")
    public R getSubDomainTest(String domain){
        List<String> list = scanService.getSubDomainTest(domain);
        return R.ok(list);
    }

    @PostMapping("masscanTest")
    public R masscanTest(@RequestBody Map<String, Object> params){
        List<String> ipList = (List<String>)params.get("ipList");
        String portParam = MapUtil.getStr(params, "port");
        Map<String, Object> result = new HashMap<>();
        for (String ip : ipList) {
            String cmd = String.format(Const.STR_MASSCAN_PORT, ip, portParam);
            log.info("执行命令：" + cmd);
            SshResponse response = null;
            try {
                response = ExecUtil.runCommand(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> scanPortList = new ArrayList<>();
            if (!StringUtils.isEmpty(response.getOut())) {
                List<String> portStrList = Arrays.asList(response.getOut().split("\n"));
                if (!CollectionUtils.isEmpty(portStrList)) {
                    for (String port : portStrList) {
                        if (!StringUtils.isEmpty(port)) {
                            port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH)).trim();
                            scanPortList.add(port);
                        }
                    }
                }
            }
            if (scanPortList.size() >= 1000) {
                result.put(ip+Const.STR_CROSSBAR + IpLongUtils.ipToLong(ip), "扫描端口超过1000，已忽略！");
            } else if (CollectionUtils.isEmpty(scanPortList)){
                result.put(ip + Const.STR_CROSSBAR + IpLongUtils.ipToLong(ip), "未扫描到端口");
            } else {
                result.put(ip + Const.STR_CROSSBAR + IpLongUtils.ipToLong(ip), scanPortList);
            }
        }
        return R.ok().put("data", result);
    }

    @PostMapping("cmsTest")
    public R cmsTest(@RequestBody Map<String, Object> params){
        String domain = MapUtil.getStr(params, "domain");
        List<Integer> portList = (List<Integer>)params.get("portList");
        List<ScanHostPortEntity> list = new ArrayList<>();
        for (Integer port : portList) {
            Map<String, String> result = null;
            try {
                result = HttpxCustomizeUtils.getUrlMap(stringRedisTemplate, toolDir, domain + Const.STR_COLON + port);
            } catch (IOException e) {
                log.error("请求错误：" + domain + Const.STR_COLON + port, e);
                e.printStackTrace();
            }
            if (!CollectionUtils.isEmpty(result)) {
                ScanHostPortEntity entity = ScanHostPortEntity.builder()
                        .domain(domain).port(port)
                        .url(result.get("url"))
                        .title(result.get("title"))
                        .cms(result.get("cms"))
                        .build();
                list.add(entity);
            }
        }
        return R.ok(list);
    }

    @GetMapping("getDomainScanPorts")
    public R getDomainScanPorts(String domain){
        return R.ok(tmpRedisService.getDomainScanPorts(domain));
    }

    @GetMapping("nucleiTest")
    public R nucleiTest(String url){
        List<Map> list = scanService.nucleiTest(url);
        return R.ok(list);
    }

    @GetMapping("nucleiSingleScan")
    public R nucleiSingleScan(Long projectId, String domain, String requestUrl, Integer tool, String nucleiParams){
        scanHoleService.nucleiSingleScan(projectId, domain, requestUrl, tool, nucleiParams);
        return R.ok();
    }

    @PostMapping("nucleiAllScan")
    public R nucleiAllScan(@RequestBody Map<String, Object> params){
        Long projectId = MapUtil.getLong(params, "projectId");
        String domain = MapUtil.getStr(params, "domain");
        List<Integer> portList = (List<Integer>)params.get("portList");
        Integer tool = MapUtil.getInt(params, "tool");
        String nucleiParams = MapUtil.getStr(params, "nucleiParams");
        scanHoleService.nucleiAllScan(projectId, domain, portList, tool, nucleiParams);
        return R.ok();
    }

    @GetMapping("afrogSingleScan")
    public R afrogSingleScan(Long projectId, String domain, String requestUrl, Integer tool, String nucleiParams){
        scanHoleService.afrogSingleScan(projectId, domain, requestUrl, tool, nucleiParams);
        return R.ok();
    }

    @PostMapping("afrogAllScan")
    public R afrogAllScan(@RequestBody Map<String, Object> params){
        Long projectId = MapUtil.getLong(params, "projectId");
        String domain = MapUtil.getStr(params, "domain");
        List<Integer> portList = (List<Integer>)params.get("portList");
        Integer tool = MapUtil.getInt(params, "tool");
        String nucleiParams = MapUtil.getStr(params, "nucleiParams");
        scanHoleService.afrogAllScan(projectId, domain, portList, tool, nucleiParams);
        return R.ok();
    }

    @PostMapping("xrayAllScan")
    public R xrayAllScan(@RequestBody Map<String, Object> params){
        Long projectId = MapUtil.getLong(params, "projectId");
        String domain = MapUtil.getStr(params, "domain");
        List<Integer> portList = (List<Integer>)params.get("portList");
        Integer tool = MapUtil.getInt(params, "tool");
        String nucleiParams = MapUtil.getStr(params, "nucleiParams");
        scanHoleService.xrayAllScan(projectId, domain, portList, tool, nucleiParams);
        return R.ok();
    }

}
