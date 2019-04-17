package com.voxlearning.utopia.service.newhomework.impl.support.processor;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.user.api.constants.UserNewHomeworkMessageType;
import lombok.Getter;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2016-10-13
 */
public abstract class UserNewHomeworkMessageAbstractProcessor extends SpringContainerSupport {

    @Inject
    protected UserNewHomeworkMessageProcessorManager userNewHomeworkMessageProcessorManager;

    @Getter
    protected UserNewHomeworkMessageType messageType;

    public void  process(Map<String,Object> map){
        if(MapUtils.isEmpty(map)){
            return;
        }
        doProcess(map);
    }

    protected abstract void doProcess(Map<String,Object> map);
}
