package com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage;

import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractSubProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage.mentalArithmetic.MentalQuestionProcessorSub;
import com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage.orcMentalArithmetic.OcrMentalArithmeticQuestionProcessorSub;

import javax.inject.Named;
import java.util.function.Consumer;

@Named
@Processors({
        MentalQuestionProcessorSub.class,
        OcrMentalArithmeticQuestionProcessorSub.class
})
public class QuestionPackageLoaderProcessor extends AbstractSubProcessor implements HomeworkProcessor {
    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        Consumer processor = subProcessor.get(param.getBizType());
        if (processor != null) {
            processor.accept(hc);
        }
    }
}
