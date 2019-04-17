package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType;
import com.voxlearning.utopia.service.afenti.api.context.FetchRankQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;

import javax.inject.Inject;
import javax.inject.Named;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class FRQD_LoadAfentiBook extends SpringContainerSupport implements IAfentiTask<FetchRankQuestionsContext> {
    @Inject private AfentiServiceImpl afentiService;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    @Override
    public void execute(FetchRankQuestionsContext context) {
        AfentiBook book = (AfentiBook) afentiService.fetchAfentiBook(context.getStudent().getId(), context.getSubject(), context.getLearningType())
                .get("book");
        if (null == book) {
            logger.error("FRQD_LoadAfentiBook Cannot load afenti book for user {}, subject {}, learningType {}",
                    context.getStudent().getId(), context.getSubject(), context.getLearningType());
            context.setErrorCode(DEFAULT.getCode());
            context.errorResponse(DEFAULT.getInfo());
            return;
        }

        if (context.getSubject() == Subject.MATH) {
            boolean isInGrayArea = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(context.getStudent(), "AfentiMath", "LimitSameGrade");
            Integer bookClazzLevel = book.book.getClazzLevel();
            if (isInGrayArea && bookClazzLevel != null && context.getStudent().getClazzLevel() != null
                    && context.getStudent().getClazzLevel().getLevel() > bookClazzLevel) {
                context.setErrorCode(AfentiErrorType.BOOK_NOT_MACH_GRADE.getCode());
                context.errorResponse(AfentiErrorType.BOOK_NOT_MACH_GRADE.getInfo());
                return;
            }
        }
        context.setBook(book);
    }
}
