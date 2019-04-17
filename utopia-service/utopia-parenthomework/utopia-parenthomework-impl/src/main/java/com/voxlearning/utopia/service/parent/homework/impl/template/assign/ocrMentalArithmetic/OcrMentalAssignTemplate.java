package com.voxlearning.utopia.service.parent.homework.impl.template.assign.ocrMentalArithmetic;


import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractProcessorTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkAssignTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.assign.*;

import javax.inject.Named;

/**
 * 纸质线下布置作业
 * @author chongfeng.qi
 * @date 20190122
 */
@Named
@Processors({
        HomeworkAssignInitProcessor.class,
        HomeworkStoreProcessor.class,
        HomeworkStoreUserRefProcessor.class,
        HomeworkBookProcessor.class,
        HomeworkAssignMQProcessor.class
})
public class OcrMentalAssignTemplate extends AbstractProcessorTemplate implements HomeworkAssignTemplate {
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
        return mapMessage.add("isAssign", true);
    }
}
