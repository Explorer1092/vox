package com.voxlearning.enanalyze;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.enanalyze.exception.BusinessException;
import com.voxlearning.utopia.enanalyze.Constant;
import com.voxlearning.utopia.enanalyze.ErrorCode;

/**
 * 结果获取工具
 *
 * @author xiaolei.li
 * @version 2018/7/6
 * @see com.voxlearning.alps.lang.util.MapMessage
 */
public abstract class MessageFetcher {

    /**
     * 从消息体中获取包装的数据
     *
     * @param message   消息
     * @param dataClazz 数据类型
     * @param <T>
     * @return
     */
    public static <T> T get(MapMessage message, Class<T> dataClazz) {
        if (null == message)
            throw new BusinessException(ViewCode.BIZ_ERROR, "消息体空");
        else if (!message.isSuccess()) {
            ErrorCode errorCode = ErrorCode.of(message.getErrorCode());
            ViewCode viewCode = ViewCode.Mapper.of(errorCode);
            throw new BusinessException(viewCode, message.getInfo());
        } else if (null == message.get(Constant.KEY_DATA))
            throw new BusinessException(ViewCode.BIZ_ERROR, String.format("消息体不包含以[%s]为键的值,消息为[%s]", Constant.KEY_DATA, message));
        return (T) message.get(Constant.KEY_DATA);
    }
}
