package com.voxlearning.utopia.agent.mockexam.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.domain.exception.SystemException;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;

import java.io.Serializable;

/**
 * 视图构建工具
 *
 * @author xiaolei.li
 * @version 2018/8/6
 */
public class ViewBuilder {

    public static final String KEY_DATA = "data";
    public static final String KEY_ERROR_DESC = "errorDesc";
    public static final String KEY_SUCCESS_DESC = "code";
    public static final String SUCCESS_MESSAGE = "成功";

    /**
     * 从dto result中获取视图模型
     *
     * @param result
     * @return
     */
    public static MapMessage fetch(Result result) {
        MapMessage message;
        if (result.isSuccess())
            message = success(result.getData());
        else {
            message = new MapMessage();
            message.setSuccess(false);
            message.setErrorCode(result.getErrorCode());
            message.setInfo(result.getErrorMessage());
            message.set(KEY_ERROR_DESC, result.getErrorMessage());
        }
        return message;
    }

    /**
     * 构建一个成功的结果
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 消息
     */
    public static <T extends Serializable> MapMessage success(T data) {
        MapMessage message = new MapMessage();
        message.setSuccess(true);
        message.set(KEY_DATA, data);
        return message;
    }

    /**
     * 构建无数据的成功消息
     *
     * @return 消息
     */
    public static MapMessage success() {
        MapMessage message = new MapMessage();
        message.setSuccess(true);
        message.set(KEY_SUCCESS_DESC, "0");
        message.setInfo(SUCCESS_MESSAGE);
        return message;
    }

    /**
     * 构建一个错误的结果,info里是面向系统用户的文案信息,errorDesc是面向开发人员的错误信息
     *
     * @param e 业务异常
     * @return 消息
     */
    public static MapMessage error(BusinessException e) {
        MapMessage message = new MapMessage();
        message.setSuccess(false);
        message.setInfo(e.getMessage());
        message.set(KEY_ERROR_DESC, e.getMessage());
        return message;
    }

    /**
     * 构建一个错误的结果
     *
     * @param e 系统异常
     * @return 消息
     */
    public static MapMessage error(SystemException e) {
        MapMessage message = new MapMessage();
        message.setSuccess(false);
        message.setInfo("服务器端发生了错误，请稍后重试");
        message.set(KEY_ERROR_DESC, e.getMessage());
        message.setInfo(e.getMessage());
        return message;
    }

    /**
     * 构建一个错误的结果
     *
     * @param e 未知异常
     * @return 消息
     */
    public static MapMessage error(Exception e) {
        MapMessage message = new MapMessage();
        message.setSuccess(false);
        message.setInfo("服务器端发生了错误，请稍后重试");
        message.set(KEY_ERROR_DESC, e.getMessage());
        message.setInfo(e.getMessage());
        return message;
    }
}
