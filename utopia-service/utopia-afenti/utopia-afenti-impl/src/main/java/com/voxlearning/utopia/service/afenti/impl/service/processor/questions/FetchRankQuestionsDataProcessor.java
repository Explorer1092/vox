package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.utopia.service.afenti.api.context.FetchRankQuestionsContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
@AfentiTasks({
        FRQD_LoadAfentiBook.class,
        FRQD_LoadAfentiOrder.class,
        FRQD_LoadNewRank.class,
        FRQD_LoadPushExamHistory.class,
        FRQD_ValidatePushExamHistory.class,
        FRQD_PushIfNecessary.class,
        FRQD_Transform.class,
        FRQD_LoadKnowledge.class,
        FRQD_RecordFootPrint.class,
        FRQD_LoadSectionShowInfo.class
})
public class FetchRankQuestionsDataProcessor extends AbstractAfentiProcessor<FetchRankQuestionsContext> {
}
