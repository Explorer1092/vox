package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.utopia.service.afenti.api.context.PushQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author ruib
 * @since 16/7/17
 */
@Named
@AfentiTasks({
        PQ_CanPush.class,
        PQ_Push.class,
        PQ_PushForPreparation.class,
        PQ_PushForNewRank.class,
        PQ_SavePush.class,
        PQ_RecordPush.class,
        PQ_PayPush.class,
        PQ_Participate.class,
})
public class PushQuestionProcessor extends AbstractAfentiProcessor<PushQuestionContext> {
}
