package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQuizType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchQuizReportContext;
import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/10/17
 */
@Named
public class QR_LoadReport extends SpringContainerSupport implements IAfentiTask<QuizResultContext> {
    @Inject private FetchQuizReportProcessor processor;

    @Override
    public void execute(QuizResultContext context) {
        if (!Boolean.TRUE.equals(context.getFinished())) return;

        AfentiQuizType type;
        String contentId;
        if (StringUtils.equals(context.getUnitId(), UtopiaAfentiConstants.CURRENT_QUIZ)) {
            type = AfentiQuizType.TERM_QUIZ;
            contentId = context.getBookId();
        } else {
            type = AfentiQuizType.UNIT_QUIZ;
            contentId = context.getUnitId();
        }

        FetchQuizReportContext in = new FetchQuizReportContext(context.getStudent(), context.getSubject(), type, contentId);
        in.setSkipStat(true);
        in.setStat(context.getStat());
        FetchQuizReportContext out = processor.process(in);
        if (out.isSuccessful()) context.getResult().putAll(out.getResult());
        context.getResult().put("score", context.getScore());
        context.getResult().put("integral", context.getIntegral());
    }
}
