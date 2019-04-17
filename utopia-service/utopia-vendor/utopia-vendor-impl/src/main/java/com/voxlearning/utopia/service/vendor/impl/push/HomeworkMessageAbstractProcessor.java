package com.voxlearning.utopia.service.vendor.impl.push;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.impl.service.AppMessageServiceImpl;
import lombok.Getter;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author xinxin
 * @since 28/7/2016
 */
public abstract class HomeworkMessageAbstractProcessor  extends SpringContainerSupport {
    @Inject
    protected HomeworkMessageProcessorManager homeworkMessageProcessorManager;
    @Inject
    protected ParentLoaderClient parentLoaderClient;
    @Inject
    protected AppMessageServiceImpl appMessageService;
    @Inject
    protected UserLoaderClient userLoaderClient;

    @Getter
    protected AppMessageSource source;

    public void process(Map<String,Object> messageMap){
        if (MapUtils.isEmpty(messageMap)){
            return;
        }

        doProcess(messageMap);
    }

    protected abstract void doProcess(Map<String, Object> messageMap);
}
