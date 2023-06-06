package com.lhh.serverbase.common.constant;

import com.lhh.serverbase.entity.SshResponse;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
     * 字符串: OK
     */
    public final static String STR_OK = "OK";
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

    public static String STR_COLON = ":";

    public static String STR_TITLE = ";";

    public static String STR_BLANK = " ";
    /**
     * 字符串：\n 换行
     */
    public final static String STR_LINEFEED = "\n";

    public static Long LONG_0 = 0L;

    public static Long LONG_3 = 3L;

    public static Integer INTEGER_0 = 0;

    public static Integer INTEGER_1 = 1;

    public static Integer INTEGER_2 = 2;

    public static Integer INTEGER_3 = 3;

    public static Integer INTEGER_4 = 4;

    public static Integer INTEGER_10 = 10;

    public static Integer INTEGER_100 = 100;

    // &&-表示前面命令执行成功在执行后面命令; ||表示前面命令执行失败了在执行后面命令; ";"表示一次执行两条命令
    public static String STR_SUBFINDER_SUBDOMAIN = "cd %s&&./subfinder -d %s -silent";

    public static String STR_MASSCAN_PORT = "masscan %s -p%s --rate 3000 --wait 0";

    public static String STR_NMAP_SERVER = "nmap -p %s %s -sS";

    public static String STR_SALT = "ac79f869b5e546e8ab6423an35b215";

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
