package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.utopia.service.afenti.api.context.FetchTermQuizReportContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
@AfentiTasks({
        FTQR_LoadQuizStat.class,
        FTQR_LoadWrongKnowledgePoints.class
})
public class FetchTermQuizReportProcessor extends AbstractAfentiProcessor<FetchTermQuizReportContext> {
}
