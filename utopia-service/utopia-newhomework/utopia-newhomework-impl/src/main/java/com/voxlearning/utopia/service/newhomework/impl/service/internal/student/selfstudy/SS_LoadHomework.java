package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/2/3
 */
@Named
public class SS_LoadHomework extends SpringContainerSupport implements SelfStudyHomeworkResultTask {

    @Inject private SelfStudyHomeworkDao selfStudyHomeworkDao;
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;

    @Override
    public void execute(SelfStudyHomeworkContext context) {
        SelfStudyHomework homework = selfStudyHomeworkDao.load(context.getHomeworkId());
        if (homework == null) {
            logger.error("SelfStudyHomework {} not found", context.getHomeworkId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            return;
        }
        NewHomework sourceHomework = newHomeworkLoader.load(homework.getSourceHomeworkId());
        if (sourceHomework != null && sourceHomework.getCreateAt().before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
            context.errorResponse("此份作业已不允许订正");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_END);
            return;
        }
        context.setSelfStudyHomework(homework);
        context.setClazzGroupId(homework.getClazzGroupId());
        context.setSubject(homework.getSubject());
    }
}
