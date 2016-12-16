package com.jason.autuman.exception;

import com.jason.autuman.enums.ExceptionCode;

/**
 * Created by yuchangcun on 2016/7/29.
 */
public class AppException extends Exception{

    private String errCode;

    private String errMsg;


    public AppException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public static AppException dbAppException(){
        return new AppException(ExceptionCode.DBERROR.getCode(),ExceptionCode.DBERROR.getDesc());
    }
    public static AppException illegalParamAppException(){
        return new AppException(ExceptionCode.ILLEGALPARAM.getCode(),ExceptionCode.ILLEGALPARAM.getDesc());
    }
    public static AppException rpcAppException(){
        return new AppException(ExceptionCode.CALLROMOTEERROR.getCode(),ExceptionCode.CALLROMOTEERROR.getDesc());
    }
    public static AppException parseErrorAppException(){
        return new AppException(ExceptionCode.PARSEERROR.getCode(),ExceptionCode.PARSEERROR.getDesc());
    }
    public static AppException internalErrorAppException(){
        return new AppException(ExceptionCode.INTERNALERROR.getCode(),ExceptionCode.INTERNALERROR.getDesc());
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
