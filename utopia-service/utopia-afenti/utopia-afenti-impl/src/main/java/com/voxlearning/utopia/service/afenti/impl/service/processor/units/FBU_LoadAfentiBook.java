package com.voxlearning.utopia.service.afenti.impl.service.processor.units;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchBookUnitsContext;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author ruib
 * @since 16/7/13
 */
@Named
public class FBU_LoadAfentiBook extends SpringContainerSupport implements IAfentiTask<FetchBookUnitsContext> {
    @Inject private AfentiServiceImpl afentiService;

    @Override
    public void execute(FetchBookUnitsContext context) {
        AfentiBook book = (AfentiBook) afentiService.fetchAfentiBook(context.getStudent().getId(), context.getSubject(), context.getLearningType())
                .get("book");
        if (null == book) {
            logger.error("FBU_LoadAfentiBook Cannot load afenti book for user {}, subject {}",
                    context.getStudent().getId(), context.getSubject());
            context.errorResponse();
            return;
        }
        context.setBook(book);
        String bookName = StringUtils.defaultString(book.book.getName());
        if (UtopiaAfentiConstants.AFENTI_BOOK_BLACK_LIST.contains(book.book.getId())) {
            bookName = StringUtils.replace(bookName, "外研版-新标准", "阿分题教材");
            bookName = StringUtils.replace(bookName, "新概念英语1(培训用)", "阿分题教材");
        }
        context.setBookName(bookName);
    }
}
