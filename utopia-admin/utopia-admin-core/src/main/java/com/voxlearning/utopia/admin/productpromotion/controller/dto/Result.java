package com.voxlearning.utopia.admin.productpromotion.controller.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据传输对象
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Data
public class Result<T extends Serializable> {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误码
     *
     * @see ErrorCode
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 数据
     */
    private T data;

    /**
     * 构建一个成功的结果
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 结果
     */
    public static <T extends Serializable> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.success = true;
        result.data = data;
        return result;
    }

    /**
     * 通过一个错误码构建一个失败的结果
     *
     * @param <T>       数据类型
     * @param errorCode 错误码
     * @return 结果
     */
    public static <T extends Serializable> Result<T> error(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.success = false;
        result.errorCode = errorCode.code;
        result.errorMessage = errorCode.desc;
        return result;
    }

    /**
     * 通过一个错误码构建一个失败的结果
     *
     * @param <T>          数据类型
     * @param errorCode    错误码
     * @param errorMessage 错误信息
     * @return 结果
     */
    public static <T extends Serializable> Result<T> error(ErrorCode errorCode, String errorMessage) {
        Result<T> result = new Result<>();
        result.success = false;
        result.errorCode = errorCode.code;
        result.errorMessage = errorMessage;
        return result;
    }

    /**
     * 通过一个错误码构建一个失败的结果
     *
     * @param errorCode 错误码
     * @param format    消息模板
     * @param args      消息参数
     * @param <T>       数据类型
     * @return 结果
     */
    public static <T extends Serializable> Result<T> error(ErrorCode errorCode, String format, Object... args) {
        Result<T> result = new Result<>();
        result.success = false;
        result.errorCode = errorCode.code;
        result.errorMessage = String.format(format, args);
        return result;
    }

    /**
     * 通过一个未捕获的异常构建一个失败的结果
     *
     * @param e   异常
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T extends Serializable> Result<T> error(Exception e) {
        Result<T> result = new Result<>();
        result.success = false;
        ErrorCode errorCode = ErrorCode.UNKNOWN;
        result.errorCode = errorCode.code;
        result.errorMessage = e.getMessage();
        return result;
    }

}
