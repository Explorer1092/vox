package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.FinishSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkResultDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@Named
public class FSS_LoadSelfStudyHomeworkResult extends SpringContainerSupport implements FinishSelfStudyHomeworkTask {

    @Inject private SelfStudyHomeworkResultDao selfStudyHomeworkResultDao;

    @Override
    public void execute(FinishSelfStudyHomeworkContext context) {
        SelfStudyHomeworkResult homeworkResult = selfStudyHomeworkResultDao.load(context.getHomeworkId());
        if (homeworkResult == null) {
            logger.error("Cannot locate student {}'s SelfStudyHomeworkResult {}", context.getUserId(), context.getHomeworkId());
            context.errorResponse();
            return;
        }
        context.setSelfStudyHomeworkResult(homeworkResult);
    }
}
