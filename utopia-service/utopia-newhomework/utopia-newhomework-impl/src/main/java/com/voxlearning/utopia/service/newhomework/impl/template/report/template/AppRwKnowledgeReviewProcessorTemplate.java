package com.voxlearning.utopia.service.newhomework.impl.template.report.template;


import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

@Named
public class AppRwKnowledgeReviewProcessorTemplate  extends AppExamObjectiveConfigTypeProcessorTemplate{

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.RW_KNOWLEDGE_REVIEW;
    }
}
