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
public class SER_LoadWrongQuestionLibrary extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {
    @Inject private WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @Override
    public void execute(ElfResultContext context) {
        if (context.getAfentiState() != AfentiState.INCORRECT2MASTER) {
            logger.error("SER_LoadWrongQuestionLibrary wrong afenti state");
            context.errorResponse();
            return;
        }

        if (context.getQuestion() == null) {
            String id = WrongQuestionLibrary.generateId(context.getStudent().getId(), context.getSubject(),
                    context.getOriginalQuestionId());
            WrongQuestionLibrary question = wrongQuestionLibraryDao.load(id);

            if (null == question) {
                logger.error("SER_LoadWrongQuestionLibrary question do not exist {} in user {} lib",
                        context.getOriginalQuestionId(), context.getStudent().getId());
                context.errorResponse();
                return;
            }
            context.setQuestion(question);
        }

        //答题答案时错的或者状态已经变更为INCORRECT2SPENDING，INCORRECT2MASTER的则停止下面流程
        if (!Boolean.TRUE.equals(context.getMaster()) ||
                context.getQuestion().getState() == AfentiState.INCORRECT2SPENDING ||
                context.getQuestion().getState() == AfentiState.INCORRECT2MASTER) context.terminateTask();
    }
}
