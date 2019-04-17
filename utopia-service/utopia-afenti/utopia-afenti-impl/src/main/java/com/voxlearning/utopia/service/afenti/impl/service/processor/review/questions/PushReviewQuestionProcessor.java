package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.utopia.service.afenti.api.context.PushReviewQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
@AfentiTasks({
        PRQ_CanPush.class,
        PRQ_Push.class,
        PRQ_SavePushResult.class,
        PRQ_RecordPushAction.class
})
public class PushReviewQuestionProcessor extends AbstractAfentiProcessor<PushReviewQuestionContext> {

}
