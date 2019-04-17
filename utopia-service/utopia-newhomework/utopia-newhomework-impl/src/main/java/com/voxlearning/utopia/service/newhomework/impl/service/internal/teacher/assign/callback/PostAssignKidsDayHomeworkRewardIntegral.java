package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.util.Arrays;

@Named
public class PostAssignKidsDayHomeworkRewardIntegral extends NewHomeworkSpringBean implements PostAssignHomework {

    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        if (NewHomeworkType.Activity == context.getNewHomeworkType() && HomeworkTag.KidsDay == context.getHomeworkTag()) {
            String comment = "老师布置儿童节主题配音获得奖励";
            IntegralHistory integralHistory = new IntegralHistory(teacher.getId(), IntegralType.PRIMARY_TEACHER_CHILDREN_DAY_HOMEWORK, 500);
            String uniqueKey = StringUtils.join(Arrays.asList(IntegralType.PRIMARY_TEACHER_CHILDREN_DAY_HOMEWORK.name(), teacher.getId()), "-");
            integralHistory.setComment(comment);
            integralHistory.setUniqueKey(uniqueKey);
            userIntegralService.changeIntegral(integralHistory);
        }
    }
}
