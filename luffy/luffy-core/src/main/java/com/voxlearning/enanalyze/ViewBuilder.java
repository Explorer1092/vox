package com.voxlearning.enanalyze;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.enanalyze.exception.BusinessException;

/**
 * 响应构建工具
 *
 * @author xiaolei.li
 * @version 2018/7/6
 * @see com.voxlearning.alps.lang.util.MapMessage
 */
public abstract class ViewBuilder {

    public final static String KEY_DATE = "data";

    /**
     * 构建一个成功的消息
     *
     * @param data 数据
     * @return 消息
     */
    public static MapMessage success(Object data) {
        return MapMessage.successMessage().add(KEY_DATE, data);
    }

    /**
     * 构建一个失败的消息
     *
     * @param e 业务异常
     * @return 消息
     */
    public static MapMessage error(BusinessException e) {
        return MapMessage.errorMessage().setErrorCode(e.getViewCode().CODE).setInfo(e.getMessage());
    }

    /**
     * 构建一个失败的消息
     *
     * @param e       业务异常
     * @param message 异常信息
     * @return 消息
     */
    public static MapMessage error(BusinessException e, String message) {
        return MapMessage.errorMessage().setErrorCode(e.getViewCode().CODE).setInfo(message);
    }

    /**
     * 构建一个失败的消息
     *
     * @param errorCode    错误码
     * @param errorMessage 错误信息
     * @return 消息
     */
    public static MapMessage error(String errorCode, String errorMessage) {
        return MapMessage.errorMessage().setErrorCode(errorCode).setInfo(errorMessage);
    }

    /**
     * 构建一个失败的消息
     *
     * @param viewCode 错误码枚举
     * @return 消息
     */
    public static MapMessage error(ViewCode viewCode) {
        return error(viewCode.CODE, viewCode.DESC);
    }

}
