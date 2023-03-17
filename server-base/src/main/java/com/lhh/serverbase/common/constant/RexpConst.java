package com.lhh.serverbase.common.constant;

public class RexpConst {

    /**
     * 判断IP格式和范围
     */
    public static String ipRex = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

    /**
     * 验证二级域名是否合法
     */
    public static String subdomainRex = "^([A-Z]|[a-z]|[0-9]|[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）――+|{}【】‘；：”“'。，、？]){6,20}$";

    /**
     * 顶级域名粗校验
     */
    public static String domainRex = "^([a-zA-Z]+.[a-zA-Z]+$)";

    /**
     * 端口输入校验
     */
    public static String portRex = "^([0-9]+$)";

    /**
     * 端口输入校验
     */
    public static String portsRex = "^([0-9]+-[0-9]+$)";

}
