package com.lhh.serverbase.common.constant;

public class CacheConst {

    /**
     * redis key: LOCK_PROJECT:
     */
    public final static String REDIS_LOCK_PROJECT = "LOCK_PROJECT:%s";

    /**
     * redis key: LOCK_PROJECT_DOMAIN_SCAN_CHANGE:
     */
    public final static String REDIS_LOCK_PROJECT_DOMAIN_SCAN_CHANGE = "LOCK_PROJECT_DOMAIN_SCAN_CHANGE:%s";

    /**
     * redis key: LOCK_DOMAIN_SCAN_CHANGE:
     */
    public final static String REDIS_LOCK_DOMAIN_SCAN_CHANGE = "LOCK_DOMAIN_SCAN_CHANGE:%s";

    /**
     * redis key: LOCK_IP_SCAN_CHANGE:
     */
    public final static String REDIS_LOCK_IP_SCAN_CHANGE = "LOCK_IP_SCAN_CHANGE:%s";

    /**
     * redis key: LOCK_IP_SCAN_RETURN:
     */
    public final static String REDIS_LOCK_IP_SCAN_RETURN = "LOCK_IP_SCAN_RETURN:%s";

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

    /**
     * redis key: REDIS_DOMAIN_COMPANY
     */
    public final static String REDIS_DOMAIN_COMPANY = "DOMAIN_COMPANY:%s";

    /**
     * redis key: REDIS_RESCAN_DOMAIN
     */
    public final static String REDIS_RESCAN_DOMAIN = "RESCAN_DOMAIN:%s";

    /**
     * redis key: REDIS_TASK_PARENT_DOMAIN
     */
    public final static String REDIS_TASK_PARENT_DOMAIN = "TASK_PARENT_DOMAIN:%s";

    /**
     * redis key: REDIS_LOCK_TASK_PARENT_DOMAIN
     */
    public final static String REDIS_LOCK_TASK_PARENT_DOMAIN = "LOCK_TASK_PARENT_DOMAIN";

    /**
     * redis key: REDIS_TASKING_IP
     */
    public final static String REDIS_TASKING_IP = "TASKING_IP:%s";

    /**
     * redis key: REDIS_NEXT_TASK
     */
    public final static String REDIS_NEXT_TASK = "NEXT_TASK";

}
