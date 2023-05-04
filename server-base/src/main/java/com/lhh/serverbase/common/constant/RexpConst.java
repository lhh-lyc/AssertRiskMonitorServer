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
     * 顶级域名校验
     */
    public static final String RE_TOP = "(gov.cn|org.cn|ac.cn|mil.cn|net.cn|edu.cn|com.cn|bj.cn|tj.cn|sh.cn|cq.cn|he.cn|sx.cn|nm.cn|ln.cn|jl.cn|hl.cn|js.cn|zj.cn|ah.cn|fj.cn|jx.cn|sd.cn|ha.cn|hb.cn|hn.cn|gd.cn|gx.cn|hi.cn|sc.cn|gz.cn|yn.cn|xz.cn|sn.cn|gs.cn|qh.cn|nx.cn|xj.cn|tw.cn|hk.cn|mo.cn|cn|ren|wang|citic|top|sohu|xin|com|net|club|xyz|vip|site|shop|ink|info|mobi|red|pro|kim|ltd|group|biz|auto|link|work|law|beer|store|tech|fun|online|art|design|wiki|love|center|video|social|team|show|cool|zone|world|today|city|chat|company|live|fund|gold|plus|guru|run|pub|email|life|co|fashion|fit|luxe|yoga|cloud|host|space|press|website|archi|asia|bio|black|blue|green|lotto|organic|pet|pink|poker|promo|ski|vote|voto|icu)";

    /**
     * 主域名校验
     */
    public static final String RE_MAJOR = "[\\w-]+\\.(gov.cn|org.cn|ac.cn|mil.cn|net.cn|edu.cn|com.cn|bj.cn|tj.cn|sh.cn|cq.cn|he.cn|sx.cn|nm.cn|ln.cn|jl.cn|hl.cn|js.cn|zj.cn|ah.cn|fj.cn|jx.cn|sd.cn|ha.cn|hb.cn|hn.cn|gd.cn|gx.cn|hi.cn|sc.cn|gz.cn|yn.cn|xz.cn|sn.cn|gs.cn|qh.cn|nx.cn|xj.cn|tw.cn|hk.cn|mo.cn|cn|ren|wang|citic|top|sohu|xin|com|net|club|xyz|vip|site|shop|ink|info|mobi|red|pro|kim|ltd|group|biz|auto|link|work|law|beer|store|tech|fun|online|art|design|wiki|love|center|video|social|team|show|cool|zone|world|today|city|chat|company|live|fund|gold|plus|guru|run|pub|email|life|co|fashion|fit|luxe|yoga|cloud|host|space|press|website|archi|asia|bio|black|blue|green|lotto|organic|pet|pink|poker|promo|ski|vote|voto|icu|baidu|中国)\\b()*";

    /**
     * 端口输入校验
     */
    public static String portRex = "^([0-9]+$)";

    /**
     * 端口输入校验
     */
    public static String portsRex = "^([0-9]+-[0-9]+$)";

}
