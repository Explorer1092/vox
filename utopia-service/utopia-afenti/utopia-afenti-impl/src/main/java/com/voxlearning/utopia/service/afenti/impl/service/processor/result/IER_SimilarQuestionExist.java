package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.impl.dao.WrongQuestionLibraryDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/24
 */
@Named
public class IER_SimilarQuestionExist extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {
    @Inject private WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @Override
    public void execute(ElfResultContext context) {

        // 如果类题存在，更改WrongQuestionLibrary状态为INCORRECT2CORRECT，将类题返回
        if (!StringUtils.equals(context.getSimilarId(), UtopiaAfentiConstants.NO_SIMILAR_QUESTION)) {
            wrongQuestionLibraryDao.updateState(context.getQuestion().getId(), AfentiState.INCORRECT2CORRECT, context.getSimilarId());
            context.getResult().put("similarId", context.getSimilarId());
            context.terminateTask();
        }
    }
}
