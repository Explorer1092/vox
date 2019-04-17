package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.consumer.cache.GroupHomeworkRecordCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType.Normal;

/**
 * 布置普通作业，缓存里面记录，key groupId  ：value 最新的作业ID
 */
@Named
public class PostAssignNewHomeworkGroupHomeworkRecord extends NewHomeworkSpringBean implements PostAssignHomework {
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;

    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        Map<Long, NewHomework> assignedHomeworks = context.getAssignedGroupHomework();
        if (MapUtils.isEmpty(assignedHomeworks)) {
            return;
        }
        GroupHomeworkRecordCacheManager groupHomeworkRecordCacheManager = newHomeworkCacheService.getGroupHomeworkRecordCacheManager();
        for (NewHomework newHomework : context.getAssignedGroupHomework().values()) {
            if (newHomework.getType() != Normal)
                continue;
            if (newHomework.getClazzGroupId() == null)
                continue;
            if (newHomework.getId() == null)
                continue;
            String cacheKey = groupHomeworkRecordCacheManager.getCacheKey(newHomework.getClazzGroupId());
            groupHomeworkRecordCacheManager.set(cacheKey, newHomework.getId());
        }
    }
}
