package com.lhh.serverbase.common.constant;

import com.alibaba.fastjson.JSON;
import com.lhh.serverbase.entity.ScanSecurityHoleEntity;
import com.lhh.serverbase.enums.LevelEnum;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Const {

    /**
     * 字符串："0"
     */
    public final static String STR_0 = "0";

    /**
     * 字符串："1"
     */
    public final static String STR_1 = "1";

    /**
     * 全范围端口字符串："1-65535"
     */
    public final static String STR_1_65535 = "1-65535";
    /**
     * 字符串：""
     */
    public final static String STR_EMPTY = "";
    /**
     * 字符串：. 逗号
     */
    public final static String STR_COMMA = ",";
    /**
     * 字符串：.点
     */
    public final static String STR_DOT = ".";
    /**
     * 字符串：. 字符点
     */
    public final static String STR_SPOT = ".";
    /**
     * 字符串: _
     */
    public final static String STR_UNDERLINE = "_";
    /**
     * 字符串：-
     */
    public final static String STR_CROSSBAR = "-";
    /**
     * 字符串：= 等于号
     */
    public final static String STR_EQUAL = "=";
    /**
     * 字符串：? 问号
     */
    public final static String STR_QUESTION = "?";
    /**
     * 字符串: /
     */
    public final static String STR_SLASH = "/";

    public final static String STR_COLON = ":";

    public final static String STR_TITLE = ";";

    public final static String STR_BLANK = " ";
    /**
     * 字符串：*号
     */
    public final static String STR_ASTERISK = "*";
    /**
     * 字符串：%号
     */
    public final static String STR_PERCENT = "%";
    /**
     * 字符串: OK
     */
    public final static String STR_OK = "OK";
    /**
     * 字符串：udp端口输入前缀
     */
    public final static String STR_U = "U:";
    /**
     * 字符串：\n 换行
     */
    public final static String STR_LINEFEED = "\n";

    public static Long LONG_0 = 0L;

    public static Long LONG_3 = 3L;

    public static Integer INTEGER_MINUS_1 = -1;

    public static Integer INTEGER_0 = 0;

    public static Integer INTEGER_1 = 1;

    public static Integer INTEGER_2 = 2;

    public static Integer INTEGER_3 = 3;

    public static Integer INTEGER_4 = 4;

    public static Integer INTEGER_5 = 5;

    public static Integer INTEGER_10 = 10;

    public static Integer INTEGER_50 = 50;

    public static Integer INTEGER_100 = 100;

    public static Integer INTEGER_200 = 200;

    public static Integer INTEGER_300 = 300;

    public static Integer INTEGER_400 = 400;

    public static Integer INTEGER_1000 = 1000;

    // &&-表示前面命令执行成功在执行后面命令; ||表示前面命令执行失败了在执行后面命令; ";"表示一次执行两条命令
    public static String STR_SUBFINDER_SUBDOMAIN = "cd %s/subfinder&&./subfinder -d %s -silent";

    public static String STR_MASSCAN_PORT = "masscan %s -p%s --rate 3000 --wait 0 -sS";

    public static String STR_NMAP_SERVER = "nmap -p %s %s -sS -Pn";

    public static String STR_FINGER = "cd %s/Finger&&python3 Finger.py -u %s -o nothing";

    /**
     * 新建批量扫描url  工具总目录  选中工具目录  多条（域名+端口\n）字符串  （文件名）projectId+domain字符串
     */
    public static String STR_CREATE_URLS = "cd %s&&./urls.sh %s %s %s.txt";

    /**
     * 删除批量扫描url  选中工具目录/urls  文件名projectId+domain字符串
     */
    public static String STR_DEL_URLS = "cd %s&&rm -rf %s.txt";

    /**
     * 单个扫描 目录 参数 域名+端口
     */
    public static String STR_NUCLEI = "cd %s/nuclei&&./nuclei -u %s %s";

    /**
     * NUCLEI批量扫描 目录 参数 域名+端口
     */
    public static String STR_NUCLEI_LIST = "cd %s/nuclei&&./nuclei -list urls/%s.txt %s";

    /**
     * 目录 参数 url
     */
    public static String STR_AFROG = "cd %s/afrog&&./afrog -t %s %s";

    /**
     * AFROG批量扫描 目录 参数 域名+端口
     */
    public static String STR_AFROG_LIST = "cd %s/afrog&&./afrog -T urls/%s.txt %s";

    /**
     * 目录 参数 域名+端口
     */
    public static String STR_XRAY = "cd %s/xray&&./xray_linux_amd64 ws --url %s %s";

    /**
     * 需要加入faviconHash.py脚本，自己写的，主要内容是拼接的url:port解析出favicon的hash
     */
    public static String STR_FAVICON_HASH = "cd %s/Finger&&python3 faviconHash.py %s";

    public static String STR_SALT = "ac79f869b5e546e8ab6423an35b215";

    public static String STR_LETTERS = "abcdefghijklmnopqrstuvwxyz";

    /**
     * 菜单类型
     */
    public enum MenuType {
        /**
         * 目录
         */
        CATALOG(0),
        /**
         * 菜单
         */
        MENU(1),
        /**
         * 按钮
         */
        BUTTON(2);

        private int value;

        MenuType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static final List<String> topList = Arrays.asList("gov.cn","org.cn","ac.cn","mil.cn","net.cn","edu.cn","com.cn","bj.cn","tj.cn","sh.cn","cq.cn","he.cn","sx.cn","nm.cn","ln.cn","jl.cn","hl.cn","js.cn","zj.cn","ah.cn","fj.cn","jx.cn","sd.cn","ha.cn","hb.cn","hn.cn","gd.cn","gx.cn","hi.cn","sc.cn","gz.cn","yn.cn","xz.cn","sn.cn","gs.cn","qh.cn","nx.cn","xj.cn","tw.cn","hk.cn","mo.cn","cn","ren","wang","citic","top","sohu","xin","com","net","club","xyz","vip","site","shop","ink","info","mobi","red","pro","kim","ltd","group","biz","auto","link","work","law","beer","store","tech","fun","online","art","design","wiki","love","center","video","social","team","show","cool","zone","world","today","city","chat","company","live","fund","gold","plus","guru","run","pub","email","life","co","fashion","fit","luxe","yoga","cloud","host","space","press","website","archi","asia","bio","black","blue","green","lotto","organic","pet","pink","poker","promo","ski","vote","voto","icu","cc");

}
