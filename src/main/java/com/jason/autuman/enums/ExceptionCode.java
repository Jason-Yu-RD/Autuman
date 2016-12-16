package com.jason.autuman.enums;

/**
 * Created by yuchangcun on 2016/7/29.
 */
public enum ExceptionCode {

    SUCCESS("200", "操作成功"),
    CALLROMOTEERROR("300","外部接口调用失败"),
    ILLEGALPARAM("400","参数校验失败"),
    PARSEERROR("401","数据转换失败"),
    INTERNALERROR("500","内部程序错误"),
    DBERROR("501","数据库操作错误");


    private String code;

    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private ExceptionCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
