package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchQuizReportContext;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiQuizType.TERM_QUIZ;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiQuizType.UNIT_QUIZ;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
public class FQR_LoadBookId extends SpringContainerSupport implements IAfentiTask<FetchQuizReportContext> {
    @Inject private AfentiServiceImpl afentiService;

    @Override
    public void execute(FetchQuizReportContext context) {
        if (context.getType() != TERM_QUIZ && context.getType() != UNIT_QUIZ) {
            logger.error("FQR_LoadAfentiBook quiz type not availale. user {}, subject {}, type",
                    context.getStudent().getId(), context.getSubject(), context.getType());
            context.errorResponse();
            return;
        }

        if (context.getType() == UNIT_QUIZ) {
            AfentiBook book = (AfentiBook) afentiService.fetchAfentiBook(context.getStudent().getId(), context.getSubject(), AfentiLearningType.castle)
                    .get("book");
            if (null == book) {
                logger.error("FQR_LoadAfentiBook Cannot load afenti book for user {}, subject {}",
                        context.getStudent().getId(), context.getSubject());
                context.errorResponse();
                return;
            }
            context.setBookId(book.book.getId());
            context.setUnitId(context.getContentId());
        } else {
            context.setBookId(context.getContentId());
            context.setUnitId(UtopiaAfentiConstants.CURRENT_QUIZ);
        }
    }
}
