package com.voxlearning.utopia.service.vendor.impl.push.processor;

import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.constant.PushTarget;
import com.voxlearning.utopia.service.push.api.constant.PushType;

import java.util.Map;
import java.util.Set;

/**
 * Created by wangshichao on 16/8/25.
 */


public interface ChannelPushProcess {

    /**
     * 由于mipush 不支持tagsAnd形式,所有把tags里面的拆出来,miPush如果tags里面有多个,则拆分成多个mipush请求
     * @param map
     * @param pushTarget
     * @return
     */
    Set<Map<String, Object>> buildSendParams(Map<String, Object> map, PushTarget pushTarget);

    PushType getPushType();

    void product(Map<String, Object> paramMap);

    /**
     * 获取第三方推送设定的包名
     * @param source
     * @return
     */
    default String getPackageName(AppMessageSource source){
        return null;
    }
}
