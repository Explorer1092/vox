package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.util.Arrays;

@Named
public class PostAssignMothersDayHomeworkRewardIntegral extends NewHomeworkSpringBean implements PostAssignHomework {

    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {

        if (NewHomeworkType.MothersDay == context.getNewHomeworkType()) {
            String comment = IntegralType.TEACHER_MOTHERS_DAY_HOMEWORK_REWARD.getDescription();
            IntegralHistory integralHistory = new IntegralHistory(teacher.getId(),
                    IntegralType.TEACHER_MOTHERS_DAY_HOMEWORK_REWARD, 500);
            String uniqueKey = StringUtils.join(Arrays.asList(IntegralType.TEACHER_MOTHERS_DAY_HOMEWORK_REWARD.name(), teacher.getId()),"-");
            integralHistory.setComment(comment);
            integralHistory.setUniqueKey(uniqueKey);
            userIntegralService.changeIntegral(integralHistory);
        }
    }
}
