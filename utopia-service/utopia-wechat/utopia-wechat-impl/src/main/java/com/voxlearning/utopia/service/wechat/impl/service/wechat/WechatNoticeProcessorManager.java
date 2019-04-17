package com.voxlearning.utopia.service.wechat.impl.service.wechat;

import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provide registration management of {@link WechatNoticeProcessor}.
 *
 * @author Xiaohai Zhang
 * @since Jan 19, 2015
 */
@Named
public class WechatNoticeProcessorManager {
    private final Map<WechatNoticeProcessorType, WechatNoticeProcessor> processors;

    public WechatNoticeProcessorManager() {
        processors = Collections.synchronizedMap(new HashMap<WechatNoticeProcessorType, WechatNoticeProcessor>());
    }

    public void register(WechatNoticeProcessor processor) {
        processors.put(processor.type(), processor);
    }

    public WechatNoticeProcessor get(WechatNoticeProcessorType type) {
        return processors.get(type);
    }
}
