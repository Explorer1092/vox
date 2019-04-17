package com.voxlearning.utopia.service.newhomework.impl.support.processor;


import com.voxlearning.utopia.service.user.api.constants.UserNewHomeworkMessageType;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2016-10-13
 */
@Named
public class UserNewHomeworkMessageProcessorManager {

    private Map<UserNewHomeworkMessageType, UserNewHomeworkMessageAbstractProcessor> processorMap = Collections.synchronizedMap(new HashMap<>());

    public void register(UserNewHomeworkMessageAbstractProcessor processor) {
        processorMap.put(processor.getMessageType(), processor);
    }

    public UserNewHomeworkMessageAbstractProcessor get(UserNewHomeworkMessageType type) {
        return processorMap.get(type);
    }
}
