package com.lhh.serverbase.common.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 返回数据
 *
 * @author Rona
 * @date 2019/4/10
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public R() {
        put("code", MsgConst.CODE_200);
        put("msg", MsgConst.MSG_MAP.get(MsgConst.CODE_200));
    }

    /**
     * 请求成功
     *
     * @return
     */
    public static R ok() {
        return new R();
    }


    /**
     * 请求成功 返回自定义msg
     *
     * @param msg
     * @return
     */
    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    /**
     * 请求成功 data 是Map
     *
     * @param map
     * @return
     */
    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    /**
     * 请求成功 返回data
     *
     * @param data
     * @return
     */
    public static R ok(Object data) {
        return new R().put("data", data);
    }


    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public R put(Object value) {
        super.put("data", value);
        return this;
    }

    /**
     * 返回错误信息
     *
     * @param code
     * @return
     */
    public static R failed(int code) {
        R r = new R();
        r.put("code", code);
        r.put("msg", MsgConst.MSG_MAP.get(code));
        return r;
    }

    /**
     * 返回错误信息 （自定义错误信息）
     *
     * @param msg
     * @return
     */
    public static R failed(String msg) {
        R r = new R();
        r.put("code", MsgConst.CODE_201);
        r.put("msg", msg);
        return r;
    }

    /**
     * 返回参数异常
     *
     * @param msgList
     * @return
     */
    public static R validError(List<String> msgList) {
        R r = new R();
        r.put("msg", msgList);
        return r;
    }

    /**
     * 返回参数异常
     *
     * @return
     */
    public static R error(String msg) {
        R r = new R();
        r.put("code", MsgConst.CODE_202);
        r.put("msg", msg);
        return r;
    }

    /**
     * 返回错误信息
     *
     * @param errCode
     * @param errMsg
     * @return
     */
    public static R error(int errCode, String errMsg) {
        R r = new R();
        r.put("code", errCode);
        r.put("msg", errMsg);
        return r;
    }

}
