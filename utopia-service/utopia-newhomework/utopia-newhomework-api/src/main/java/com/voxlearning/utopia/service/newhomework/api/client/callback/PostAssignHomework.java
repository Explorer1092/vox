package com.voxlearning.utopia.service.newhomework.api.client.callback;

import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/6
 */
public interface PostAssignHomework {
    void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context);
}
