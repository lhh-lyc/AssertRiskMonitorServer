package com.lhh.servermonitor.utils;

import com.lhh.serverbase.common.constant.RexpConst;
import com.lhh.serverbase.utils.Const;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PortUtils {

    public static String getNewPorts(String oldPorts, String newPorts) {
        List<String> oldPortList = getPortList(oldPorts);
        List<String> newPortList = getPortList(newPorts);
        oldPortList.addAll(newPortList);
        List<Integer> list = oldPortList.stream().distinct().map(Integer::parseInt).collect(Collectors.toList());
        List<List<Integer>> resultList = new ArrayList<>();
        List<Integer> arrList = new ArrayList<>();
        if (list.size() == 1) {
            resultList.get(resultList.size() - 1).add(list.get(0));
        } else {
            for (int i = 0; i < list.size(); i++) {
                Integer nextNum = list.get(i + 1);
                Integer nowNum = list.get(i);
                if (nextNum - nowNum != 1) {
                    resultList.get(resultList.size() - 1).add(nowNum);
                    arrList = new ArrayList<>();
                    resultList.add(arrList);
                } else {
                    arrList.add(nowNum);
                }
                if (i + 1 == list.size() - 1) {
                    arrList.add(nextNum);
                    resultList.add(arrList);
                    break;
                }
            }
        }
        List<String> portList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(resultList)) {
            for (List<Integer> i : resultList) {
                if (Const.INTEGER_1.equals(i.size())) {
                    portList.add(String.valueOf(i));
                } else {
                    portList.add(i.get(0) + Const.STR_CROSSBAR + i.get(i.size() - 1));
                }
            }
        }
        return String.join(Const.STR_COMMA, portList);
    }

    public static Boolean portEquals(String oldPorts, String newPorts) {
        List<String> oldPortList = getPortList(oldPorts);
        List<String> newPortList = getPortList(newPorts);
        oldPortList.sort(Comparator.comparing(String::hashCode));
        newPortList.sort(Comparator.comparing(String::hashCode));
        return oldPortList.toString().equals(newPortList.toString());
    }

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

    public static void main(String[] args) {
        System.out.println("65535-".matches("^([0-9]*-[1-9]*$)"));
    }

}
