package com.voxlearning.utopia.service.vendor.impl.push;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.vendor.api.constant.HomeworkVendorMessageType;
import com.voxlearning.utopia.service.vendor.consumer.JxtServiceClient;
import lombok.Getter;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016-9-12
 */
public abstract class NewHomeworkVendorMessageAbstractProcessor extends SpringContainerSupport {

    @Inject
    protected NewHomeworkVendorMessageProcessorManager newHomeworkVendorMessageProcessorManager;
    @Inject
    protected JxtServiceClient jxtServiceClient;

    @Getter
    protected HomeworkVendorMessageType messageType;

    public void process(Map<String, Object> messageMap) {
        if (MapUtils.isEmpty(messageMap)) {
            return;
        }

        doProcess(messageMap);
    }

    protected abstract void doProcess(Map<String, Object> messageMap);
}
