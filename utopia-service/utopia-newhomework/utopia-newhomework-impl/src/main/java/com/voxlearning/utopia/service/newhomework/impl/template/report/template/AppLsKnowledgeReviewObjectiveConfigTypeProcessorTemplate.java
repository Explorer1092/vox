package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

@Named
public class AppLsKnowledgeReviewObjectiveConfigTypeProcessorTemplate extends AppBasicAppObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.LS_KNOWLEDGE_REVIEW;
    }
}
