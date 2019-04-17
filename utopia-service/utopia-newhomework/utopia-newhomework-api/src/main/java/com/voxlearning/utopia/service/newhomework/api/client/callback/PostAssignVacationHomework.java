package com.voxlearning.utopia.service.newhomework.api.client.callback;

import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

/**
 * Created by tanguohong on 2016/12/6.
 */
public interface PostAssignVacationHomework {
    void afterVacationHomeworkAssigned(Teacher teacher, AssignVacationHomeworkContext context);
}
