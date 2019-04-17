package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.AfentiState;
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
public class SER_ChangeState extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {
    @Inject private WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @Override
    public void execute(ElfResultContext context) {
        wrongQuestionLibraryDao.updateState(context.getQuestion().getId(), AfentiState.INCORRECT2MASTER, context.getQuestionId());
        context.getIds().add(context.getQuestion().getId());
    }
}
