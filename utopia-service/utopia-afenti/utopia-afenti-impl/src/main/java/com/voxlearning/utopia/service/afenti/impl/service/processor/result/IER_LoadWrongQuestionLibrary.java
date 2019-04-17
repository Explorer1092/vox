package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.dao.WrongQuestionLibraryDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/24
 */
@Named
public class IER_LoadWrongQuestionLibrary extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {
    @Inject private WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @Override
    public void execute(ElfResultContext context) {
        if (context.getAfentiState() != AfentiState.INCORRECT2CORRECT) {
            logger.error("IER_LoadWrongQuestionLibrary wrong afenti state");
            context.errorResponse();
            return;
        }

        String id = WrongQuestionLibrary.generateId(context.getStudent().getId(), context.getSubject(), context.getQuestionId());
        WrongQuestionLibrary question = wrongQuestionLibraryDao.load(id);

        if (null == question) {
            logger.error("IER_LoadWrongQuestionLibrary question do not exist {} in user {} lib",
                    context.getQuestionId(), context.getStudent().getId());
            context.errorResponse();
            return;
        }
        //答题的答案错误或者状态已经由INCORRECT变更为其他状态则停止下面流程
        if (!Boolean.TRUE.equals(context.getMaster()) || question.getState() != AfentiState.INCORRECT) context.terminateTask();
        context.setQuestion(question);
    }
}
