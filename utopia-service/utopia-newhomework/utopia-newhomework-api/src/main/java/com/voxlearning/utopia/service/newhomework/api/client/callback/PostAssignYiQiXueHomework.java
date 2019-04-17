package com.voxlearning.utopia.service.newhomework.api.client.callback;

import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

/**
 * Created by tanguohong on 2017/7/11.
 */
public interface PostAssignYiQiXueHomework {
    void afterYiQiXueHomeworkAssigned(Teacher teacher, AssignHomeworkContext context);
}
