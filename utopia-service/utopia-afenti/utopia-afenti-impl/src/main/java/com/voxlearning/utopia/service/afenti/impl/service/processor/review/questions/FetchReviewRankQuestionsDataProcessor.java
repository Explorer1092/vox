package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.utopia.service.afenti.api.context.FetchReviewQuestionsContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
@AfentiTasks({
        FRRQD_LoadAfentiBook.class,
        FRRQD_LoadAfentiOrder.class,
        FRRQD_LoadPushExamHistory.class,
        FRRQD_PushIfNecessary.class,
        FRRQD_LoadQuestionKnowledge.class,
        FRRQD_Transform.class
})
public class FetchReviewRankQuestionsDataProcessor extends AbstractAfentiProcessor<FetchReviewQuestionsContext> {
}
