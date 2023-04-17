package com.lhh.serverbase.common.constant;

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

    public static Long LONG_0 = 0L;

    public static Long LONG_3 = 3L;

    public static Integer INTEGER_0 = 0;

    public static Integer INTEGER_1 = 1;

    public static Integer INTEGER_2 = 2;

    public static Integer INTEGER_3 = 3;

    public static Integer INTEGER_4 = 4;

    public static Integer INTEGER_100 = 100;

    // &&-表示前面命令执行成功在执行后面命令; ||表示前面命令执行失败了在执行后面命令; ";"表示一次执行两条命令
    public static String STR_SUBFINDER_SUBDOMAIN = "cd /mnt/webSafe/utils/subfinder&&./subfinder -d %s -silent";

    public static String STR_MASSCAN_PORT = "masscan %s -p%s --rate %s --wait 0";

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

}
