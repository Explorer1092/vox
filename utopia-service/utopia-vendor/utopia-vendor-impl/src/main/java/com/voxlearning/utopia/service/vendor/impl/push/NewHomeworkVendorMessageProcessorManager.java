package com.voxlearning.utopia.service.vendor.impl.push;

import com.voxlearning.utopia.service.vendor.api.constant.HomeworkVendorMessageType;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016-9-12
 */
@Named
public class NewHomeworkVendorMessageProcessorManager {

    private Map<Integer, NewHomeworkVendorMessageAbstractProcessor> processorMap = Collections.synchronizedMap(new HashMap<>());

    public void register(NewHomeworkVendorMessageAbstractProcessor processor) {
        processorMap.put(processor.getMessageType().getType(), processor);
    }

    public NewHomeworkVendorMessageAbstractProcessor get(HomeworkVendorMessageType messageType) {
        return processorMap.get(messageType.getType());
    }
}
