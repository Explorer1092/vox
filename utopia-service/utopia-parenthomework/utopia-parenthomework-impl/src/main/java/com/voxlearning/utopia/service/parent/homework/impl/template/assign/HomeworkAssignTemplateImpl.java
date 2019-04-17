package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SupportType;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractProcessorTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkAssignTemplate;

import javax.inject.Named;

/**
 * @author chongfeng.qi
 * @date 20181120
 */
@Named
@Processors({
        HomeworkAssignInitProcessor.class,
        HomeworkStoreProcessor.class,
        HomeworkStoreUserRefProcessor.class,
        HomeworkAssign2BigDataMQProcessor.class,
        HomeworkAssignMQProcessor.class
})
@SupportType(bizType = "*",op="assign")
public class HomeworkAssignTemplateImpl extends AbstractProcessorTemplate implements HomeworkAssignTemplate {

    /**
     * 布置作业
     * @param param
     * @return
     */
    public MapMessage assign(HomeworkParam param) {
        HomeworkContext hc = new HomeworkContext();
        hc.setHomeworkParam(param);
        // 流程处理
        processor.accept(hc);
        MapMessage mapMessage = hc.getMapMessage();
        if (mapMessage == null) {
            mapMessage = MapMessage.successMessage();
        }
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        return mapMessage.add("isAssign", true).add("homeworkId", hc.getHomework().getId());
    }
}
