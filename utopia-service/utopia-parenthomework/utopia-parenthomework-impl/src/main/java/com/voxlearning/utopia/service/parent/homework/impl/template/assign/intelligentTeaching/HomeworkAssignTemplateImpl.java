package com.voxlearning.utopia.service.parent.homework.impl.template.assign.intelligentTeaching;


import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SupportType;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractProcessorTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkAssignTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.assign.HomeworkAssignMQProcessor;

import javax.inject.Named;

/**
 * 布置作业
 *
 * @author Wenlong Meng
 * @since Feb 19, 2019
 */
@Named("IntelliagentTeaching.HomeworkAssignTemplateImpl")
@Processors({
        HomeworkAssignInitProcessor.class,
        HomeworkStoreProcessor.class,
        HomeworkUserRefStoreProcessor.class,
        HomeworkAssignMQProcessor.class
})
@SupportType(
        bizType = "INTELLIGENT_TEACHING",
        op="assign"
)
public class HomeworkAssignTemplateImpl extends AbstractProcessorTemplate implements HomeworkAssignTemplate {
    /**
     * 布置作业
     * @param param
     * @return
     */
    @Override
    public MapMessage assign(HomeworkParam param) {
        HomeworkContext hc = new HomeworkContext();
        hc.setHomeworkParam(param);
        // 流程处理
        processor.accept(hc);
        MapMessage mapMessage = hc.getMapMessage();
        if (mapMessage == null) {
            mapMessage = MapMessage.successMessage();
        }
        if(mapMessage.isSuccess()){
            mapMessage.add("isAssign", true).set("homeworkId", hc.getHomework().getId());
        }
        return mapMessage;
    }
}
