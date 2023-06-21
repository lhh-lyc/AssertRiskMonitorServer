package com.lhh.serverbase.utils;

import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.constant.RexpConst;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    private static Set<Integer> parsePorts(String s) {
        if (StringUtils.isEmpty(s)) {
            return new HashSet<>();
        }
        Set<Integer> set = new HashSet<>();
        String[] ranges = s.split(",");
        for (String r : ranges) {
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

    public static String getNewPorts(String s1, String s2) {
        Set<Integer> set1 = parsePorts(s1);
        Set<Integer> set2 = parsePorts(s2);

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

    public static Boolean portEquals(String oldPorts, String newPorts) {
        Set<Integer> set1 = parsePorts(oldPorts);
        Set<Integer> set2 = parsePorts(newPorts);
        return set1.equals(set2);
    }

}
