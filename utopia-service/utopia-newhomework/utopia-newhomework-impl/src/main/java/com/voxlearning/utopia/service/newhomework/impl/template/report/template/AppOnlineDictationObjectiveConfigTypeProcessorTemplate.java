package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

/**
 * @Description: 线上听写
 * @author: Mr_VanGogh
 * @date: 2019/1/23 下午5:34
 */
@Named
public class AppOnlineDictationObjectiveConfigTypeProcessorTemplate extends AppExamObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ONLINE_DICTATION;
    }
}