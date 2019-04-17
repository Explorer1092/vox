package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.unit;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchQuizInfoContext;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/10/13
 */
@Named
public class FQI_LoadAfentiBook extends SpringContainerSupport implements IAfentiTask<FetchQuizInfoContext> {
    @Inject private AfentiServiceImpl afentiService;

    @Override
    public void execute(FetchQuizInfoContext context) {
        if (StringUtils.equals(context.getUnitId(), UtopiaAfentiConstants.ULTIMATE_UNIT)) {
            context.getResult().put("exist", false);
            context.terminateTask();
        }

        AfentiBook book = (AfentiBook) afentiService.fetchAfentiBook(context.getStudent().getId(), context.getSubject(), AfentiLearningType.castle)
                .get("book");
        if (null == book) {
            logger.error("FQI_LoadAfentiBook Cannot load afenti book for user {}, subject {}",
                    context.getStudent().getId(), context.getSubject());
            context.errorResponse();
            return;
        }
        context.setBook(book);
    }
}
