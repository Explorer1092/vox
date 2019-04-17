package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

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
import java.util.LinkedHashSet;
import java.util.Set;

@Named
public class PostAssignNewHomeworkUpdatePictureBookPlusHistory extends NewHomeworkSpringBean implements PostAssignHomework {
    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        Set<String> pictureBookPlusIds = new LinkedHashSet<>();
        for (NewHomework newHomework : context.getAssignedGroupHomework().values()) {
            NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.LEVEL_READINGS);
            if (newHomeworkPracticeContent != null && CollectionUtils.isNotEmpty(newHomeworkPracticeContent.getApps())) {
                newHomeworkPracticeContent.getApps()
                        .stream()
                        .filter(app -> StringUtils.isNotBlank(app.getPictureBookId()))
                        .forEach(app -> pictureBookPlusIds.add(app.getPictureBookId()));
            }
        }
        if (CollectionUtils.isNotEmpty(pictureBookPlusIds)) {
            pictureBookPlusHistoryDao.updatePictureBookPlusHistory(teacher.getId(), teacher.getSubject(), pictureBookPlusIds);
        }
    }
}
