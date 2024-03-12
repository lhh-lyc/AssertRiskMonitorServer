package com.lhh.serverbase.common.constant;

public class CacheConst {

    /**
     * redis key: 新建任务增加/完成主域名加锁
     */
    public final static String REDIS_LOCK_PROJECT = "LOCK_PROJECT:%s";

    /**
     * redis key: 主域名扫描
     */
    public final static String REDIS_LOCK_DOMAIN = "LOCK_DOMAIN:%s";

    /**
     * redis key: 域名扫描
     */
    public final static String REDIS_LOCK_DOMAIN_PORT = "LOCK_DOMAIN_PORT:%s";

    /**
     * redis key: LOCK_SUBDOMAIN:
     */
    public final static String REDIS_LOCK_SUBDOMAIN = "LOCK_SUBDOMAIN:%s";

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
     * redis key: LOCK_SCANNING_IP:
     */
    public final static String REDIS_LOCK_SCANNING_IP = "LOCK_SCANNING_IP:%s";

    /**
     * redis key: LOCK_HOST_PORT
     */
    public final static String REDIS_LOCK_HOST_PORT = "LOCK_HOST_PORT:%s";

    /**
     * redis key: LOCK_RESCAN_PROJECT:
     */
    public final static String REDIS_LOCK_RESCAN_PROJECT = "LOCK_RESCAN_PROJECT:%s";

    /**
     * redis key: hostInfo信息更新
     */
    public final static String REDIS_LOCK_HOST_INFO = "LOCK_HOST_INFO:%s";

    /**
     * redis key: LOCK_UPDATE_PROJECT_HOST
     */
    public final static String REDIS_LOCK_UPDATE_PROJECT_HOST = "LOCK_UPDATE_PROJECT_HOST:%s:%s";

    /**
     * redis key: LOCK_UPDATE_PROJECT_HOST
     */
    public final static String REDIS_LOCK_UPDATE_HOST = "LOCK_UPDATE_PROJECT_HOST:%s";

    /**
     * redis key: LOCK_UPDATE_PROJECT_PORT
     */
    public final static String REDIS_LOCK_UPDATE_PORT = "LOCK_UPDATE_PROJECT_PORT:%s";

    /**
     * redis key: SCANNING_PROJECT:
     */
    public final static String REDIS_SCANNING_PROJECT = "SCANNING_PROJECT:%s";

    /**
     * redis key: SCANNING_DOMAIN:projectId:domain
     */
    public final static String REDIS_SCANNING_DOMAIN = "SCANNING_DOMAIN:%s:%s";

    /**
     * redis key: SCANNING_DOMAIN_PORT:projectId:domain
     */
    public final static String REDIS_SCANNING_DOMAIN_PORT = "SCANNING_DOMAIN_PORT:%s:%s:%s";

    /**
     * redis key: SCANNING_SUB_DOMAIN:
     */
    public final static String REDIS_SCANNING_SUB_DOMAIN = "SCANNING_SUB_DOMAIN:%s";

    /**
     * redis key: SCANNING_IP:
     */
    public final static String REDIS_SCANNING_IP = "SCANNING_IP:%s";

    /**
     * redis key: SCANNING_PROJECT:
     */
    public final static String REDIS_RESCAN_PROJECT = "RESCAN_PROJECT:%s";

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
     * redis key: REDIS_HOST_INFO
     */
    public final static String REDIS_HOST_INFO = "HOST_INFO:%s";

    /**
     * redis key: REDIS_DOMAIN_COMPANY
     */
    public final static String REDIS_DOMAIN_COMPANY = "DOMAIN_COMPANY:%s";

    /**
     * redis key: REDIS_DOMAIN_COMPANY
     */
    public final static String REDIS_DOMAIN_SCANPORTS = "DOMAIN_SCANPORTS:%s";

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
     * redis key: REDIS_BEGIN_TASK
     */
    public final static String REDIS_BEGIN_TASK = "BEGIN_TASK";

    /**
     * redis key: REDIS_TASKING_IP
     */
    public final static String REDIS_TASKING_IP = "TASKING_IP:%s";

    /**
     * 主域名定时扫描: REDIS_NEXT_TASK
     */
    public final static String REDIS_NEXT_TASK = "NEXT_TASK";

    /**
     * redis key: REDIS_CMS_JSON
     */
    public final static String REDIS_CMS_JSON = "CMS_JSON";

    /**
     * redis key: REDIS_CMS_JSON_LIST (keyword)
     */
    public final static String REDIS_CMS_JSON_LIST = "CMS_JSON_LIST";

    /**
     * redis key: REDIS_CMS_JSON_MAP (faviconhash)
     */
    public final static String REDIS_CMS_JSON_MAP = "CMS_JSON_MAP";

    /**
     * redis key: 表示已经处理过的rabbitmq消息（由于可能消费时间太长，导致30分钟后连接断开然后一直重复消费）
     * 消费时先判断有没有该缓存，有就说明已经消费过了，直接确认
     */
    public final static String REDIS_END_DOMAIN = "END_DOMAIN:%s";

    /**
     * redis key: 表示已经处理过的rabbitmq消息（由于可能消费时间太长，导致30分钟后连接断开然后一直重复消费）
     * 消费时先判断有没有该缓存，有就说明已经消费过了，直接确认
     */
    public final static String REDIS_END_HOST_PORT = "END_HOST_PORT:%s";

    /**
     * redis key: 天数（新建任务中过期则重新扫描）
     */
    public final static String REDIS_VAIL_DAY = "VAIL_DAY";

    /**
     * redis key: 备案信息查询
     */
    public final static String REDIS_COMPANY_QUERY = "COMPANY_QUERY";

    /**
     * redis key: 扫描漏洞服务名
     */
    public final static String REDIS_SERVER_NAMES = "SERVER_NAMES";

    /**
     * redis key: 扫描漏洞服务名
     */
    public final static String REDIS_PROJECT_STATISTICS_NUM = "ARMS:PROJECT_STATISTICS_NUM:%S";

}
