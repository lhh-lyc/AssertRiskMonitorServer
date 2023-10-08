package com.lhh.serverbase.common.constant;

/**
 * JWT相关的常量
 *
 * @author kisang
 * @date 2020-1-19
 */
public class JwtConst {
    //----JWT开始----
    /**
     * JWT签名密钥：PC端
     */
    public final static String JWT_SRCRET = "dV79AVOplnRhOMw4yr7Du05Nte54VBzd";
    /**
     * JWT签名access_token有效期(PC端):60分钟(单位秒)
     */
    public final static long JWT_ACCESS_EXPIRE_TIME = 60 * 60;

    /**
     * JWT签名refresh_token有效期:2小时(单位秒)
     */
    public final static int JWT_REFRESH_EXPIRE_TIME = 2 * 60 * 60;

    /**
     * JWT签名refresh_token被删除保留临时信息:10分钟(单位秒)
     */
    public final static int JWT_DEL_REFRESH_EXPIRE_TIME = 10 * 60;

    /**
     * JWT签名refresh_token刷新时缓冲时间15秒(单位秒)
     */
    public final static int JWT_REFRESH_EXPIRE_BUFFER_TIME = 15;

    /**
     * Shiro缓存过期时间-60分钟(秒为单位)(一般设置与AccessToken过期时间一致)
     */
    public final static int JWT_SHIRO_CACHE_EXPIRE_TIME = 60 * 60;

    /**
     * JWT签名key:currentTimeMillis
     */
    public final static String JWT_KEY_CURRENTTIMEMILLIS = "currentTimeMillis";
    /**
     * JWT签名key:userName
     */
    public final static String JWT_KEY_USERNAME = "userName";

    /**
     * JWT签名key:Authorization
     */
    public final static String JWT_KEY_HEAD = "Authorization";

    /**
     * JWT签名key:userId
     */
    public final static String JWT_KEY_USERID = "userId";

    /**
     * JWT签名key:platType
     */
    public final static String JWT_KEY_PLAT_TYPE = "platType";

    /**
     * JWT签名key:platType
     */
    public final static String JWT_KEY_DEV_CODE = "devCode";

    /**
     * JWT临时删除key前缀:del
     */
    public final static String JWT_KEY_DEL = "del:";

    //----JWT结束----

    //----平台类型开始----
    /**
     * 平台类型 - admin（后台管理系统）
     * 0 后台管理系统， 1 移动端
     */
    public final static int PLAT_TYPE_ADMIN = 0;

    /**
     * 平台类型 - app（移动端）
     * 0 后台管理系统， 1 移动端
     */
    public final static int PLAT_TYPE_APP = 1;
    public final static String PLAT_TYPE_APP_STR = "1";
    //----平台类型结束----

    //--redisKey前缀字符 开始--
    /**
     * 前缀字符: admin:refresh_token:
     */
    public final static String PREFIX_ADMIN_REFRESH_TOKEN = "dy:admin:refresh_token:";
    /**
     * 前缀字符: shiro:admin:cache:
     */
    public final static String PREFIX_ADMIN_SHIRO_CACHE = "dy:admin:shiro:cache:";
    /**
     * 前缀字符: app:refresh_token:
     */
    public final static String PREFIX_APP_REFRESH_TOKEN = "dy:app:refresh_token:";
    /**
     * 前缀字符: shiro:app:cache:
     */
    public final static String PREFIX_APP_SHIRO_CACHE = "dy:app:shiro:cache:";
    /**
     * 前缀字符：tmp
     */
    public final static String PREFIX_TMP = "tmp:";
    // --redisKey前缀字符 结束--

    // -- minio相关 --
    /**
     * minio超时设置
     */
    public final static long MINIO_EXPIRE_TIME = 5 * 60 * 1000;
    // -- minio结束 --
}
