package com.voxlearning.utopia.service.vendor.impl.push;

import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 28/7/2016
 */
@Named
public class HomeworkMessageProcessorManager {
    private final Map<String,HomeworkMessageAbstractProcessor> processors = Collections.synchronizedMap(new HashMap<>());

    public void regist(HomeworkMessageAbstractProcessor processor){
        processors.put(processor.getSource().name(),processor);
    }

    public HomeworkMessageAbstractProcessor get(AppMessageSource source){
        return processors.get(source.name());
    }
}
