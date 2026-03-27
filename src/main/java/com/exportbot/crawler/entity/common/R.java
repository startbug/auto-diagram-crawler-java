package com.exportbot.crawler.entity.common;

import com.exportbot.crawler.enums.ResponseCode;

import lombok.Data;

/**
 * @Author starbug
 * @Description
 * @Datetime 2026/3/27 23:23
 */
@Data
public class R<T> {

    private String msg;

    private Integer code;

    private T data;

    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(ResponseCode.SUCCESS.getCode());
        r.setMsg(ResponseCode.SUCCESS.getDesc());
        r.setData(data);
        return r;
    }

    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.setCode(ResponseCode.FAIL.getCode());
        r.setMsg(msg);
        r.setData(null);
        return r;
    }

    public static <T> R<T> error(Integer code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }

    public static <T> R<T> error(ResponseCode responseCode) {
        R<T> r = new R<>();
        r.setCode(responseCode.getCode());
        r.setMsg(responseCode.getDesc());
        r.setData(null);
        return r;
    }

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(ResponseCode.SUCCESS.getCode());
        r.setMsg(ResponseCode.SUCCESS.getDesc());
        r.setData(null);
        return r;
    }

}
