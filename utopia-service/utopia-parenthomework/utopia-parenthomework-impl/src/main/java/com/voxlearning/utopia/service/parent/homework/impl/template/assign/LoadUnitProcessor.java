package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractSubProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.assign.ocrMentalArithmetic.OcrLoadUnitProcessorSub;

import javax.inject.Named;
import java.util.function.Consumer;

/**
 * 获取单元
 * @author chongfeng.qi
 * @data 20190112
 */
@Named
@Processors({
        LoadUnitProcessorSub.class,
        OcrLoadUnitProcessorSub.class
})
public class LoadUnitProcessor extends AbstractSubProcessor implements HomeworkProcessor {

    @Override
    public void process(HomeworkContext hc) {
        Consumer processor = subProcessor.get(hc.getHomeworkParam().getBizType());
        if (processor != null) {
            processor.accept(hc);
        }
    }
}
