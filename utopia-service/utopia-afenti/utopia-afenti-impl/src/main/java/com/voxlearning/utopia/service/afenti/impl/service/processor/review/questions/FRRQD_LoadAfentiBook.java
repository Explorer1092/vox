package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class FRRQD_LoadAfentiBook extends SpringContainerSupport implements IAfentiTask<FetchReviewQuestionsContext> {
    @Inject
    private AfentiServiceImpl afentiService;

    @Override
    public void execute(FetchReviewQuestionsContext context) {
        AfentiBook book = (AfentiBook) afentiService.fetchAfentiBook(context.getStudent().getId(), context.getSubject(), AfentiLearningType.review)
                .get("book");
        if (book == null) {
            logger.error("FRRQD_LoadAfentiBook Cannot load afenti book for user {}, subject {}",
                    context.getStudent().getId(), context.getSubject());
            context.setErrorCode(DEFAULT.getCode());
            context.errorResponse(DEFAULT.getInfo());
            return;
        }

        context.setBook(book);
    }
}
