package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractSubProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.assign.mentalArithmetic.LoadQuestionPackageProcessorSub;
import com.voxlearning.utopia.service.parent.homework.impl.template.assign.ocrMentalArithmetic.LoaderQuestionProcessorSub;

import javax.inject.Named;
import java.util.function.Consumer;

/**
 * 获取题题包
 * @author chongfeng.qi
 * @version 20181111
 * @since 2018-11-21
 */
@Named
@Processors({
        ExamLoaderQuestionPackageProcessorSub.class,
        LoadQuestionPackageProcessorSub.class,
        LoaderQuestionProcessorSub.class
})
public class HomeworkLoadQuestionPackageProcessor extends AbstractSubProcessor implements HomeworkProcessor {

    @Override
    public void process(HomeworkContext hc) {
        Consumer processor = subProcessor.get(hc.getHomeworkParam().getBizType());
        if (processor != null) {
            processor.accept(hc);
        }
    }
}
