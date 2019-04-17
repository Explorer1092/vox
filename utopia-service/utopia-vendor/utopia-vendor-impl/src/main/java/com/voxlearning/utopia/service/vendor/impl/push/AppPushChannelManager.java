package com.voxlearning.utopia.service.vendor.impl.push;

import com.voxlearning.utopia.service.push.api.constant.PushType;
import com.voxlearning.utopia.service.vendor.impl.push.processor.ChannelPushProcess;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by wangshichao on 16/8/25.
 */

@Named
public class AppPushChannelManager {


    private final Map<PushType, ChannelPushProcess> processors
            = Collections.synchronizedMap(new HashMap<>());

    public void register(ChannelPushProcess process) {
        Objects.requireNonNull(process);
        processors.put(process.getPushType(), process);
    }

    public ChannelPushProcess get(PushType source) {
        return processors.get(source);
    }
}
