package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.utopia.service.afenti.api.context.FetchQuizReportContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
@AfentiTasks({
        FQR_LoadBookId.class,
        FQR_LoadReportTitle.class,
        FQR_LoadQuizStat.class,
        FQR_LoadWrongKnowledgePoints.class,
})
public class FetchQuizReportProcessor extends AbstractAfentiProcessor<FetchQuizReportContext> {
}
