package com.lhh.serverscanhole.utils;

import com.lhh.serverbase.common.exception.EmException;
import com.lhh.serverbase.common.constant.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.*;

/**
 * JedisUtils(推荐存Byte数组，存Json字符串效率更慢)
 *
 * @author Wang926454
 * @date 2018/9/4 15:45
 */
@Component
@Slf4j
public class JedisUtils {


    /**
     * 静态注入JedisPool连接池
     * 本来是正常注入JedisUtils，可以在Controller和Service层使用，但是重写Shiro的CustomCache无法注入JedisUtils
     * 现在改为静态注入JedisPool连接池，JedisUtils直接调用静态方法即可
     * https://blog.csdn.net/W_Z_W_888/article/details/79979103
     */
    private static JedisPool jedisPool;

    @Autowired
    public void setJedisPool(JedisPool jedisPool) {
        JedisUtils.jedisPool = jedisPool;
    }

    /**
     * 获取Jedis实例
     *
     * @param
     * @return redis.clients.jedis.Jedis
     */
    public static synchronized Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new EmException("获取Jedis资源异常:" + e.getMessage());
        }
    }

    /**
     * 释放Jedis资源
     *
     * @param
     * @return void
     * @author Wang926454
     * @date 2018/9/5 9:16
     */
    public static void closePool() {
        try {
            jedisPool.close();
        } catch (Exception e) {
            throw new EmException("释放Jedis资源异常:" + e.getMessage());
        }
    }

    /**
     * 获取redis键值-object
     *
     * @param key
     * @return java.lang.Object
     * @author Wang926454
     * @date 2018/9/4 15:47
     */
    public static Object getObject(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[] bytes = jedis.get(key.getBytes());
            if (!StringUtils.isEmpty(bytes)) {
                return SerializableUtils.unserializable(bytes);
            }
        } catch (Exception e) {
            throw new EmException("获取Redis键值getObject方法异常:key=" + key + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 设置redis键值-object
     *
     * @param key
     * @param value
     * @return java.lang.String
     * @author Wang926454
     * @date 2018/9/4 15:49
     */
    public static String setObject(String key, Object value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.set(key.getBytes(), SerializableUtils.serializable(value));
        } catch (Exception e) {
            throw new EmException("设置Redis键值setObject方法异常:key=" + key + " value=" + value + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 设置redis键值-object-expiretime
     *
     * @param key
     * @param value
     * @param expiretime
     * @return java.lang.String
     * @author Wang926454
     * @date 2018/9/4 15:50
     */
    public static String setObject(String key, Object value, int expiretime) {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.set(key.getBytes(), SerializableUtils.serializable(value));
            if (Const.STR_OK.equals(result)) {
                jedis.expire(key.getBytes(), expiretime);
            }
            return result;
        } catch (Exception e) {
            throw new EmException("设置Redis键值setObject方法异常:key=" + key + " value=" + value + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 获取redis键值-Json
     *
     * @param key
     * @return java.lang.Object
     * @author Wang926454
     * @date 2018/9/4 15:47
     */
    public static String getJson(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.get(key);
        } catch (Exception e) {
            throw new EmException("获取Redis键值getJson方法异常:key=" + key + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 设置redis键值-Json
     *
     * @param key
     * @param value
     * @return java.lang.String
     * @author Wang926454
     * @date 2018/9/4 15:49
     */
    public static String setJson(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.set(key, value);
        } catch (Exception e) {
            throw new EmException("设置Redis键值setJson方法异常:key=" + key + " value=" + value + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 设置redis键值-Json-expiretime
     *
     * @param key
     * @param value
     * @param expiretime
     * @return java.lang.String
     * @author Wang926454
     * @date 2018/9/4 15:50
     */
    public static String setJson(String key, String value, int expiretime) {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.set(key, value);
            if (Const.STR_OK.equals(result)) {
                jedis.expire(key, expiretime);
            }
            return result;
        } catch (Exception e) {
            throw new EmException("设置Redis键值setJson方法异常:key=" + key + " value=" + value + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 删除key
     *
     * @param key
     * @return java.lang.Long
     * @author Wang926454
     * @date 2018/9/4 15:50
     */
    public static Long delKey(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.del(key.getBytes());
        } catch (Exception e) {
            throw new EmException("删除Redis的键delKey方法异常:key=" + key + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * key是否存在
     *
     * @param key
     * @return java.lang.Boolean
     * @author Wang926454
     * @date 2018/9/4 15:51
     */
    public static Boolean exists(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(key.getBytes());
        } catch (Exception e) {
            throw new EmException("查询Redis的键是否存在exists方法异常:key=" + key + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 模糊查询获取key集合
     *
     * @param key
     * @return java.util.Set<java.lang.String>
     * @author Wang926454
     * @date 2018/9/6 9:43
     */
    public static Set<String> keysS(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.keys(key);
        } catch (Exception e) {
            throw new EmException("模糊查询Redis的键集合keysS方法异常:key=" + key + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 模糊查询获取key集合
     *
     * @param key
     * @return java.util.Set<java.lang.String>
     * @author Wang926454
     * @date 2018/9/6 9:43
     */
    public static Set<byte[]> keysB(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.keys(key.getBytes());
        } catch (Exception e) {
            throw new EmException("模糊查询Redis的键集合keysB方法异常:key=" + key + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 获取过期剩余时间
     *
     * @param key
     * @return java.lang.String
     * @author Wang926454
     * @date 2018/9/11 16:26
     */
    public static Long getExpireTime(String key) {
        Long result = -2L;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.ttl(key);
            return result;
        } catch (Exception e) {
            throw new EmException("获取Redis键过期剩余时间getExpireTime方法异常:key=" + key + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 获取redis键值-object
     *
     * @param key
     * @return java.lang.Object
     * @author Wang926454
     * @date 2018/9/4 15:47
     */
    public static String getStr(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[] bytes = jedis.get(key.getBytes());
            if (!StringUtils.isEmpty(bytes)) {
                return new String(bytes);
            }
        } catch (Exception e) {
            throw new EmException("获取Redis键值getObject方法异常:key=" + key + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return Const.STR_EMPTY;
    }

    /**
     * 获取锁操作
     *
     * @param lockKey
     * @param lockValue
     * @param expireTime
     * @return
     */
    public static boolean getLock(String lockKey, String lockValue, int expireTime) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Long result = jedis.setnx(lockKey, lockValue);
            //获取锁成功
            if (result == 1) {
                //程序在此处崩溃会有死锁的情况
                jedis.expire(lockKey, expireTime);
                return true;
            }
        } catch (Exception e) {
            log.info("获取加锁异常:key=" + lockKey + " cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    /**
     * 批量查询
     * @param keyList
     * @return
     */
    public static Map<String, String> getPipeJson(List<String> keyList) {
        Jedis jedis = null;
        if (CollectionUtils.isEmpty(keyList)) {
            return new HashMap<>();
        }
        HashMap<String, Response<String>> intrmMap = new HashMap<String, Response<String>>();
        Map<String, String> map = new HashMap<>();
        try {
            jedis = jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            for (String key : keyList) {
                intrmMap.put(key, pipeline.get(key));
            }
            pipeline.sync();
            for (Map.Entry<String, Response<String>> entry :intrmMap.entrySet()) {
                Response<String> sResponse = (Response<String>)entry.getValue();
                String key = new String(entry.getKey());
                String value = sResponse.get();
                map.put(key, value);
            }
        } catch (Exception e) {
            log.error("设置Redis键值setPipeJson方法异常 cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return map;
    }

    /**
     * 设置redis键值-Json
     *
     * @return java.lang.String
     * @author Wang926454
     * @date 2018/9/4 15:49
     */
    public static String setPipeJson(Map<String, String> map) {
        Jedis jedis = null;
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        try {
            jedis = jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            for (String key : map.keySet()) {
                pipeline.set(key, map.get(key));
            }
            pipeline.sync();
        } catch (Exception e) {
            log.error("设置Redis键值setPipeJson方法异常 cause=" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public static boolean pipeDel(List<String> list){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            for (String key : list) {
                //批量删除
                pipeline.del(key);
            }
            pipeline.sync();//同步
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if (jedis != null) {
                jedis.close();
            }
            return false;
        }
    }

}
