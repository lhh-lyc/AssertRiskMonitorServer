package com.lhh.servermonitor.service;

import com.lhh.serverbase.entity.SshResponse;
import com.lhh.serverbase.common.constant.Const;
import com.lhh.servermonitor.utils.ExecUtil;
import com.lhh.serverbase.utils.RexpUtil;
import com.lhh.servermonitor.utils.SshConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExecService {

    public void test(List<String> ipList, String ports) {
        Long time1 = System.currentTimeMillis();
        List<Future<List<Integer>>> list = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        Future<List<Integer>> future = executor.submit(new Callable<List<Integer>>() {
            @Override
            public List<Integer> call() {
                List<Integer> portList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(ipList)) {
                    for (String host : ipList) {
                        portList.addAll(getPortList(host, ports));
                    }
                }
                return portList;
            }
        });
        list.add(future);

        // 拼接返回数据
        for (int i = 0; i < list.size(); i++) {
            future = list.get(i);
            while (!future.isDone()) { // 这一行代码很重要
                try {
                    System.out.println("aaaaaaaaaaaaa线程" + i + "," + future.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        Long time2 = System.currentTimeMillis();
        System.out.println(time2 - time1);

        Long time3 = System.currentTimeMillis();
        List<Integer> portList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ipList)) {
            for (String host : ipList) {
                portList.addAll(getPortList(host, ports));
            }
        }
        Long time4 = System.currentTimeMillis();
        System.out.println(time4 - time3);
    }

    /**
     * 利用工具查询子域名
     */
    public List<String> getDomainList(String domain) {
        // 子域名列表
        List<String> subdomainList = new ArrayList<>();
        if (RexpUtil.isSubDomain(domain)) {
            subdomainList.add(domain);
        } else {
            // &&-表示前面命令执行成功在执行后面命令; ||表示前面命令执行失败了在执行后面命令; ";"表示一次执行两条命令
            String cmd = "cd /mnt/webSafe/utils/subfinder/;./subfinder -d %s -silent";
            cmd = String.format(cmd, domain);
            SshResponse response = null;
            try {
                response = ExecUtil.runCommand(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("=================");
            System.out.println(response.getOut());
            subdomainList = Arrays.asList(response.getOut().split("\n"));
        }

        // 子域名解析ip
        List<String> ipList = getDomainIpList(subdomainList);
        return ipList;
    }

    /**
     * java代码解析子域名ip
     */
    public List<String> getDomainIpList(List<String> domainNameList) {
        List<String> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(domainNameList)) {
            for (String domainName : domainNameList) {
                System.out.println("使用InetAddress类的方法获取网站" + domainName + "的IP地址...");
                try {
                    System.out.println("总共ip个数："
                            + InetAddress.getAllByName(domainName).length);//获取接续出来的ip的个数
                    InetAddress[] inetadd = InetAddress.getAllByName(domainName);
                    //遍历所有的ip并输出
                    for (int i = 0; i < inetadd.length; i++) {
                        list.add(inetadd[i] + "");
                        System.out.println("第" + (i + 1) + "个ip：" + inetadd[i]);
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }


    /**
     * java代码获取开放端口
     */
    public List<Integer> getPortList(String host, String ports) {
        List<Integer> portList = new ArrayList<>();
        String cmd = String.format(Const.STR_MASSCAN_PORT, host, ports, 5000);
        SshResponse response = null;
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> portStrList = Arrays.asList(response.getOut().split("\n"));
        if (!CollectionUtils.isEmpty(portStrList)) {
            for (String port : portStrList) {
                port = port.substring(port.indexOf("port ") + 5, port.indexOf(Const.STR_SLASH));
                portList.add(Integer.valueOf(port));
            }
        }
        portList.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        return portList;
    }

    public List<String> getServerList(String host, int startPort, int endPort) {
        String ip = "49.235.104.19";
        String ports = "6379,8086";
        //    &&-表示前面命令执行成功在执行后面命令; ||表示前面命令执行失败了在执行后面命令; ";"表示一次执行两条命令
        String cmd = "cd /mnt/webSafe/utils/;nmap -p %s %s -sS";
        cmd = String.format(cmd, ports, ip);
        SshResponse response = null;
        try {
            response = ExecUtil.runCommand(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> portList = Arrays.asList(ports.split(Const.STR_COMMA));
        List<String> responseLineList = Arrays.asList(response.getOut().split("\n"));
        List<String> serverLineList = responseLineList.stream().filter(r -> !StringUtils.isEmpty(r) && r.contains(Const.STR_SLASH) && portList.contains(r.substring(0, r.indexOf(Const.STR_SLASH)))).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(serverLineList)) {
            for (String server : serverLineList) {
                System.out.println(ip + Const.STR_CROSSBAR + server.substring(0, server.indexOf(Const.STR_SLASH)) + Const.STR_COLON + server.substring(server.lastIndexOf(Const.STR_BLANK)));
            }
        }
        return serverLineList;
    }

    /**
     * 利用工具查询子域名
     */
    public Integer getCpuNum() {
        if (SshConnection.getIsProd()) {
            String cmd = "grep 'physical id' /proc/cpuinfo | sort -u | wc -l";
            cmd = String.format(cmd);
            SshResponse response = null;
            try {
                response = ExecUtil.runCommand(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Integer cpuNum = Integer.valueOf(response.getOutList().get(0));
            cmd = "grep 'core id' /proc/cpuinfo | sort -u | wc -l";
            cmd = String.format(cmd);
            response = null;
            try {
                response = ExecUtil.runCommand(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Integer nuclearNum = Integer.valueOf(response.getOutList().get(0));
            return cpuNum * nuclearNum;
        } else {
            return Runtime.getRuntime().availableProcessors();
        }
    }

}
