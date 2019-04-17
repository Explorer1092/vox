package com.voxlearning.utopia.enanalyze;

import com.voxlearning.alps.lang.util.MapMessage;

import static com.voxlearning.utopia.enanalyze.Constant.KEY_DATA;

/**
 * 消息构建器
 *
 * @author xiaolei.li
 * @version 2018/7/21
 * @see MapMessage
 */
public abstract class MessageBuilder {

    /**
     * 构建一个成功的消息
     *
     * @param data 数据
     * @return
     */
    public static MapMessage success(Object data) {
        return MapMessage.successMessage().add(KEY_DATA, data);
    }

    /**
     * 构建一个失败的消息
     *
     * @param errorCode    错误码
     * @param errorMessage 错误信息
     * @return
     */
    public static MapMessage error(String errorCode, String errorMessage) {
        return MapMessage.errorMessage().setErrorCode(errorCode).setInfo(errorMessage);
    }

    /**
     * 构建一个失败的消息
     *
     * @param errorCode 错误码枚举
     * @return
     */
    public static MapMessage error(ErrorCode errorCode) {
        return error(errorCode.CODE, errorCode.DESC);
    }
}
