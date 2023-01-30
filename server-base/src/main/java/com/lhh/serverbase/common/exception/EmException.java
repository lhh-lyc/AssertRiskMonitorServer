package com.lhh.serverbase.common.exception;


import com.lhh.serverbase.common.response.MsgConst;

/**
 * 自定义异常
 *
 * @author Rona
 * @date 2019/4/10
 */
public class EmException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String msg;
    private int code = MsgConst.CODE_201;

    public EmException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public EmException(int code) {
        this.code = code;
        this.msg = MsgConst.MSG_MAP.get(code);
    }

    public EmException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public EmException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public EmException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


}
