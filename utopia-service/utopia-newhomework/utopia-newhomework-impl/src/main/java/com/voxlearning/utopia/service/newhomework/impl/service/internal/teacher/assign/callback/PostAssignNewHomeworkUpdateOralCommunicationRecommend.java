package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/4
 * \* Time: 3:41 PM
 * \* Description:口语交际布置记录
 * \
 */
@Named
public class PostAssignNewHomeworkUpdateOralCommunicationRecommend extends NewHomeworkSpringBean implements PostAssignHomework {
    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        List<String> stoneIds= Lists.newArrayList();
        for (NewHomework newHomework : context.getAssignedGroupHomework().values()) {
            NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.ORAL_COMMUNICATION);
            if (newHomeworkPracticeContent != null && CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())) {
                newHomeworkPracticeContent.getApps()
                        .stream()
                        .filter(app -> StringUtils.isNotBlank(app.getStoneDataId()))
                        .forEach(app -> stoneIds.add(app.getStoneDataId()));
            }
        }
        if (CollectionUtils.isNotEmpty(stoneIds)) {
            oralCommunicationRecommendRecordDao.updateOralCommunicationHistory(teacher.getId(), teacher.getSubject(), stoneIds);
        }

    }
}
