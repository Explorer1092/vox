package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewAccomplishmentDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2017/8/30
 */
@Named
public class HR_CheckHomeworkFinished extends SpringContainerSupport implements HomeworkResultTask {
    @Inject private NewAccomplishmentDao newAccomplishmentDao;

    @Override
    public void execute(HomeworkResultContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        String id = NewAccomplishment.ID.build(location.getCreateTime(), location.getSubject(), location.getId()).toString();
        NewAccomplishment newAccomplishment = newAccomplishmentDao.load(id);
        boolean finished = newAccomplishment != null && newAccomplishment.contains(context.getUserId());
        if (finished) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_FINISHED,
                    "mod3", context.getObjectiveConfigType(),
                    "mod4", JsonUtils.toJson(context.getStudentHomeworkAnswers()),
                    "op", "student homework result"
            ));
            context.errorResponse("练习已完成，请返回到练习记录查看");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_FINISHED);
        }
    }
}
