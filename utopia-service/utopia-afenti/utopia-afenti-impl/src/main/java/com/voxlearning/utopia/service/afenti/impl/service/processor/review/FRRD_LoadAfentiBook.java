package com.voxlearning.utopia.service.afenti.impl.service.processor.review;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRanksContext;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/11/28
 */
@Named
public class FRRD_LoadAfentiBook extends SpringContainerSupport implements IAfentiTask<FetchReviewRanksContext> {
    @Inject private AfentiServiceImpl afentiService;

    @Override
    public void execute(FetchReviewRanksContext context) {
        AfentiBook book = (AfentiBook) afentiService.fetchAfentiBook(context.getStudent().getId(), context.getSubject(), AfentiLearningType.review)
                .get("book");
        if (null == book) {
            logger.error("FRRD_LoadAfentiBook Cannot load afenti book for user {}, subject {}",
                    context.getStudent().getId(), context.getSubject());
            context.errorResponse();
            return;
        }
        context.setBook(book);
        context.setBookName(StringUtils.defaultString(book.book.getName()));
    }
}
