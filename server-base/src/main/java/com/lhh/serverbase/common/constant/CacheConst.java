package com.lhh.serverbase.common.constant;

public class CacheConst {

    /**
     * redis key: LOCK_PROJECT:
     */
    public final static String REDIS_LOCK_PROJECT = "LOCK_PROJECT:%s";

    /**
     * redis key: SCANNING_PROJECT:
     */
    public final static String REDIS_SCANNING_PROJECT = "SCANNING_PROJECT:%s";

    /**
     * redis key: SCANNING_DOMAIN:
     */
    public final static String REDIS_SCANNING_DOMAIN = "SCANNING_DOMAIN:%s";

    /**
     * redis key: SCANNING_IP:
     */
    public final static String REDIS_SCANNING_IP = "SCANNING_IP:%s";

    /**
     * redis key: TASK_HOST_ID
     */
    public final static String REDIS_TASK_HOST_ID = "TASK_HOST_ID";

    /**
     * 是否校验垂直越权（关掉方便测试接口）
     * redis key: IS_SELF_VALID
     */
    public final static String REDIS_IS_SELF_VALID = "IS_SELF_VALID";

}
