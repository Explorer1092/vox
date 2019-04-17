package com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage;

import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractProcessorTemplate;

import javax.inject.Named;

/**
 *
 * 获取口算题包
 * @author chongfeng.qi
 * @data 20190116
 *
 */
@Named("QuestionPackageTemplate")
@Processors({
        QuestionPackageInitProcessor.class,
        QuestionPackageLoaderProcessor.class
})
public class QuestionPackageTemplate extends AbstractProcessorTemplate {

    public void questionPackage(HomeworkContext hc) {
        processor.accept(hc);
    }
}
