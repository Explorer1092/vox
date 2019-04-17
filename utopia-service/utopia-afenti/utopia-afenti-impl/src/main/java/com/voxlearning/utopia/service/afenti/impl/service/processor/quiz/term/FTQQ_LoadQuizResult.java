package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchTermQuizQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/10/19
 */
@Named
public class FTQQ_LoadQuizResult extends SpringContainerSupport implements IAfentiTask<FetchTermQuizQuestionContext> {
    @Inject private AfentiLoaderImpl afentiLoader;
    @Inject protected NewContentLoaderClient newContentLoaderClient;

    @Override
    public void execute(FetchTermQuizQuestionContext context) {
        if (context.getStudent().getClazz() == null) {
            logger.error("FTQQ_LoadQuizResult student {} do not chose a clazz", context.getStudent().getId());
            context.errorResponse();
            return;
        }

        Long studentId = context.getStudent().getId();
        String bookId = context.getBookId();
        String unitId = UtopiaAfentiConstants.CURRENT_QUIZ;

        List<AfentiQuizResult> qrs = afentiLoader.loadAfentiQuizResultByUserIdAndNewBookId(studentId, bookId)
                .stream()
                .filter(r -> StringUtils.equals(r.getNewUnitId(), unitId))
                .filter(r -> r.getSubject() == context.getSubject())
                .collect(Collectors.toList());
        context.setQrs(qrs);
    }
}
