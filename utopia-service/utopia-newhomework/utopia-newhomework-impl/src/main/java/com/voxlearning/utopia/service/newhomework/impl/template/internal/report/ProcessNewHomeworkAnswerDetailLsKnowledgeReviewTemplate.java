package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

@Named
public class ProcessNewHomeworkAnswerDetailLsKnowledgeReviewTemplate extends ProcessNewHomeworkAnswerDetailBasicAppTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.LS_KNOWLEDGE_REVIEW;
    }
}
