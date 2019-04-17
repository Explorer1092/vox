package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCHR_LoadHomework extends SpringContainerSupport implements LiveCastHomeworkResultTask {

    @Inject private LiveCastHomeworkDao liveCastHomeworkDao;

    @Override
    public void execute(LiveCastHomeworkResultContext context) {

        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(context.getHomeworkId());
        if (liveCastHomework == null) {
            logger.error("LiveCastHomework {} not found", context.getHomeworkId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            return;
        }

        Subject subject = liveCastHomework.getSubject();
        if (Objects.equals(subject, Subject.UNKNOWN) || liveCastHomework.getType().getTypeId() != NewHomeworkType.ThirdPartyType) {
            logger.error("LiveCastHomework {} type error", context.getHomeworkId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
            return;
        }

        context.setLiveCastHomework(liveCastHomework);
        context.setNewHomeworkType(liveCastHomework.getType());
        context.setClazzGroupId(liveCastHomework.getClazzGroupId());
        context.setSubject(subject);
    }
}
