package com.voxlearning.utopia.service.afenti.impl.service.processor.video.prepraration;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchPreparationVideoContext;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author songtao
 * @since 17/7/20
 */
@Named
public class FPV_LoadAfentiBook extends SpringContainerSupport implements IAfentiTask<FetchPreparationVideoContext> {
    @Inject private AfentiServiceImpl afentiService;

    @Override
    public void execute(FetchPreparationVideoContext context) {
        AfentiBook book = (AfentiBook) afentiService.fetchAfentiBook(context.getStudent().getId(), context.getSubject(), AfentiLearningType.preparation)
                .get("book");

        if (null == book) {
            logger.error("FPV_LoadAfentiBook Cannot load afenti book for user {}", context.getStudent().getId());
            context.errorResponse();
            return;
        }
        context.setBook(book);

        String bookName = StringUtils.defaultString(book.book.getName());
        context.setBookName(bookName);
    }

}
