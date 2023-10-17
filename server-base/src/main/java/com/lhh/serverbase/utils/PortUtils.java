package com.lhh.serverbase.utils;

import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.constant.RexpConst;
import com.lhh.serverbase.entity.ScanPortEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class PortUtils {

    public static List<String> getPortList(String ports) {
        List<String> portList = new ArrayList<>();
        if (!StringUtils.isEmpty(ports)) {
            List<String> list = Arrays.asList(ports.split(Const.STR_COMMA));
            for (String port : list) {
                if (port.matches(RexpConst.portRex)) {
                    portList.add(port);
                }
                if (port.matches(RexpConst.portsRex)) {
                    String begin = port.split(Const.STR_CROSSBAR)[0];
                    String end = port.split(Const.STR_CROSSBAR)[1];
                    for (int i = Integer.valueOf(begin); i <= Integer.valueOf(end); i++) {
                        portList.add(String.valueOf(i));
                    }
                }
            }
        }
        return portList;
    }

    private static Map<String, Set<Integer>> parsePorts(String s) {
        if (StringUtils.isEmpty(s) || Const.STR_CROSSBAR.equals(s)) {
            return new HashMap<>();
        }
        List<String> ranges = new ArrayList<>(Arrays.asList(s.split(",")));
        List<String> tList = new ArrayList<>();
        List<String> uList = new ArrayList<>();
        for (String r : ranges) {
            if (RexpUtil.isUdpPort(r.toLowerCase())) {
                uList.add(r.toLowerCase().replace("u:", Const.STR_EMPTY));
                continue;
            }
            if (RexpUtil.isTcpPort(r)){
                tList.add(r);
                continue;
            }
        }
        Set<Integer> tSet = splitPorts(tList);
        Set<Integer> uSet = splitPorts(uList);
        Map<String, Set<Integer>> map = new HashMap<>();
        map.put("tSet", tSet);
        map.put("uSet", uSet);
        return map;
    }

    public static Set<Integer> splitPorts(List<String> list){
        Set<Integer> set = new HashSet<>();
        for (String r : list) {
            String[] parts = r.split("-");
            if (parts.length == 1) {
                int port = Integer.parseInt(parts[0]);
                set.add(port);
            } else {
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                for (int port = start; port <= end; port++) {
                    set.add(port);
                }
            }
        }
        return set;
    }

    public static Boolean portEquals(String oldPorts, String newPorts) {
        Map<String, Set<Integer>> map1 = parsePorts(oldPorts);
        Map<String, Set<Integer>> map2 = parsePorts(newPorts);
        Set<Integer> tSet1 = CollectionUtils.isEmpty(map1.get("tSet")) ? new HashSet<>() : map1.get("tSet");
        Set<Integer> tSet2 = CollectionUtils.isEmpty(map2.get("tSet")) ? new HashSet<>() : map2.get("tSet");
        Set<Integer> uSet1 = CollectionUtils.isEmpty(map1.get("uSet")) ? new HashSet<>() : map1.get("uSet");
        Set<Integer> uSet2 = CollectionUtils.isEmpty(map2.get("uSet")) ? new HashSet<>() : map2.get("uSet");
        tSet2.removeAll(tSet1);
        uSet2.removeAll(uSet1);
        return CollectionUtils.isEmpty(tSet2) && CollectionUtils.isEmpty(uSet2);
    }


    /**
     * 获取所有扫描的端口（已扫描端口+本次扫描端口）
     * @param oldPorts
     * @param newPorts
     * @return
     */
    public static String getAllPorts(String oldPorts, String newPorts) {
        Map<String, Set<Integer>> map1 = parsePorts(oldPorts);
        Map<String, Set<Integer>> map2 = parsePorts(newPorts);
        Set<Integer> tSet1 = CollectionUtils.isEmpty(map1.get("tSet")) ? new HashSet<>() : map1.get("tSet");
        Set<Integer> tSet2 = CollectionUtils.isEmpty(map2.get("tSet")) ? new HashSet<>() : map2.get("tSet");
        Set<Integer> uSet1 = CollectionUtils.isEmpty(map1.get("uSet")) ? new HashSet<>() : map1.get("uSet");
        Set<Integer> uSet2 = CollectionUtils.isEmpty(map2.get("uSet")) ? new HashSet<>() : map2.get("uSet");
        String tcpPorts = getAllTcpPorts(tSet1, tSet2);
        String udpPorts = getAllUdpPorts(uSet1, uSet2);
        return StringUtils.isEmpty(tcpPorts) ? udpPorts : StringUtils.isEmpty(udpPorts) ? tcpPorts : tcpPorts + Const.STR_COMMA + udpPorts;
    }

    public static String getAllTcpPorts(Set<Integer> set1, Set<Integer> set2) {
        Set<Integer> tmpSet = new HashSet<>();
        tmpSet.addAll(set1);
        tmpSet.addAll(set2);
        Set<Integer> union = new TreeSet<>(tmpSet);
        if (union.isEmpty()) {
            return "";
        }
        if (union.size() == 1) {
            return union.iterator().next().toString();
        }

        List<String> ranges = new ArrayList<>();
        int start = union.iterator().next();
        int end = start;
        for (int port : union) {
            if (port <end+1) {
                continue;
            }
            if (port == end + 1) {
                end = port;
            } else {
                ranges.add(start == end ? String.valueOf(end) : start + Const.STR_CROSSBAR + end);
                start = end = port;
            }
        }
        ranges.add(start == end ? String.valueOf(end) : start + Const.STR_CROSSBAR + end);
        return String.join(",", ranges);
    }

    public static String getAllUdpPorts(Set<Integer> set1, Set<Integer> set2) {
        Set<Integer> tmpSet = new HashSet<>();
        tmpSet.addAll(set1);
        tmpSet.addAll(set2);
        Set<Integer> union = new TreeSet<>(tmpSet);
        if (union.isEmpty()) {
            return "";
        }
        if (union.size() == 1) {
            return union.iterator().next().toString();
        }

        List<String> ranges = new ArrayList<>();
        int start = union.iterator().next();
        int end = start;
        for (int port : union) {
            if (port <end+1) {
                continue;
            }
            if (port == end + 1) {
                end = port;
            } else {
                ranges.add(start == end ? Const.STR_U + end : Const.STR_U + start + Const.STR_CROSSBAR + end);
                start = end = port;
            }
        }
        ranges.add(start == end ? Const.STR_U + end : Const.STR_U + start + Const.STR_CROSSBAR + end);
        return String.join(",", ranges);
    }

    /**
     * 获取本次扫描的新端口（本次扫描端口-已扫描端口）
     * @param oldPorts
     * @param newPorts
     * @return
     */
    public static String getNewPorts(String oldPorts, String newPorts) {
        Map<String, Set<Integer>> map1 = parsePorts(oldPorts);
        Map<String, Set<Integer>> map2 = parsePorts(newPorts);
        Set<Integer> tSet1 = CollectionUtils.isEmpty(map1.get("tSet")) ? new HashSet<>() : map1.get("tSet");
        Set<Integer> tSet2 = CollectionUtils.isEmpty(map2.get("tSet")) ? new HashSet<>() : map2.get("tSet");
        Set<Integer> uSet1 = CollectionUtils.isEmpty(map1.get("uSet")) ? new HashSet<>() : map1.get("uSet");
        Set<Integer> uSet2 = CollectionUtils.isEmpty(map2.get("uSet")) ? new HashSet<>() : map2.get("uSet");
        String tcpPorts = getNewTcpPorts(tSet1, tSet2);
        String udpPorts = getNewUdpPorts(uSet1, uSet2);
        return StringUtils.isEmpty(tcpPorts) ? udpPorts : StringUtils.isEmpty(udpPorts) ? tcpPorts : tcpPorts + Const.STR_COMMA + udpPorts;
    }

    public static String getNewTcpPorts(Set<Integer> set1, Set<Integer> set2) {
        set2.removeAll(set1);
        Set<Integer> union = new TreeSet<>(set2);
        if (union.isEmpty()) {
            return "";
        }
        if (union.size() == 1) {
            return union.iterator().next().toString();
        }

        List<String> ranges = new ArrayList<>();
        int start = union.iterator().next();
        int end = start;
        for (int port : union) {
            if (port <end+1) {
                continue;
            }
            if (port == end + 1) {
                end = port;
            } else {
                ranges.add(start == end ? String.valueOf(end) : start + Const.STR_CROSSBAR + end);
                start = end = port;
            }
        }
        ranges.add(start == end ? String.valueOf(end) : start + Const.STR_CROSSBAR + end);
        return String.join(",", ranges);
    }

    public static String getNewUdpPorts(Set<Integer> set1, Set<Integer> set2) {
        set2.removeAll(set1);
        Set<Integer> union = new TreeSet<>(set2);
        if (union.isEmpty()) {
            return "";
        }
        if (union.size() == 1) {
            return union.iterator().next().toString();
        }

        List<String> ranges = new ArrayList<>();
        int start = union.iterator().next();
        int end = start;
        for (int port : union) {
            if (port <end+1) {
                continue;
            }
            if (port == end + 1) {
                end = port;
            } else {
                ranges.add(start == end ? Const.STR_U + end : Const.STR_U + start + Const.STR_CROSSBAR + end);
                start = end = port;
            }
        }
        ranges.add(start == end ? Const.STR_U + end : Const.STR_U + start + Const.STR_CROSSBAR + end);
        return String.join(",", ranges);
    }

    /**
     * 只过滤保留本次扫描的端口数据
     * @param portList
     * @param scanPorts
     * @return
     */
    public static List<ScanPortEntity> filterPortList(List<ScanPortEntity> portList, String scanPorts){
        if (CollectionUtils.isEmpty(portList)) {
            return portList;
        }
        Map<String, Set<Integer>> map = parsePorts(scanPorts);
        Set<Integer> tSet = CollectionUtils.isEmpty(map.get("tSet")) ? new HashSet<>() : map.get("tSet");
        Set<Integer> uSet = CollectionUtils.isEmpty(map.get("uSet")) ? new HashSet<>() : map.get("uSet");
        List<ScanPortEntity> tList = portList.stream().filter(p->tSet.contains(p.getPort())).collect(Collectors.toList());
        return tList;
    }

}
