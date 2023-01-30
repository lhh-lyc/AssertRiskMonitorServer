package com.lhh.serverbase.common.response;

import java.util.Map;
import java.util.TreeMap;

/**
 * 返回Code、Msg定义类
 *
 * @author Rona
 * @date 2019/4/10
 */
public class MsgConst {

    /**
     * 请求成功
     */
    public static final Integer CODE_200 = 200;

    /**
     * 请求失败
     */
    public static final Integer CODE_201 = 201;

    /**
     * 参数不完整
     */
    public static final Integer CODE_202 = 202;

    /**
     * 该企业账号已存在，请重新填写
     */
    public static final Integer CODE_204 = 204;

    /**
     * 请先删除子菜单或按钮
     */
    public static final Integer CODE_205 = 205;

    /**
     * 未认证，请先登录
     */
    public static final Integer CODE_401 = 401;

    /**
     * 登录失效，请重新登录！
     */
    public static final Integer CODE_402 = 402;

    /**
     * 没有权限，请确认！
     */
    public static final Integer CODE_403 = 403;

    /**
     * 下载报表失败
     */
    public static final Integer CODE_404 = 404;

    /**
     * 上报通道号失败
     */
    public static final Integer CODE_405 = 405;

    /**
     * 微信授权失败
     */
    public static final Integer CODE_406 = 406;

    /**
     * 当前平台不能执行该操作
     */
    public static final Integer CODE_407 = 407;

    /**
     * 该车牌号同时存在于车辆维护和申请单中，已从车辆维护中删除
     */
    public static final Integer CODE_408 = 408;

    /**
     * 该车牌号已存在于申请单中
     */
    public static final Integer CODE_409 = 409;


    /**
     * 提示信息
     */
    public final static Map<Integer, String> MSG_MAP = new TreeMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(CODE_200, "请求成功");
            put(CODE_201, "请求失败");
            put(CODE_202, "参数不完整");
            put(CODE_204, "该企业账号已存在，请重新填写");
            put(CODE_205, "请先删除子菜单或按钮");
            put(CODE_401, "未认证，请先登录！");
            put(CODE_402, "登录失效，请重新登录！");
            put(CODE_403, "没有权限,请确认！");
            put(CODE_404, "下载失败！");
            put(CODE_405, "上报通道号失败");
            put(CODE_406, "微信授权失败");
            put(CODE_407, "当前平台不能执行该操作");
            put(CODE_408, "该车牌号同时存在于车辆维护和申请单中，已从车辆维护中删除！");
            put(CODE_409, "该车牌号已存在于申请单中！");
        }
    };

}
