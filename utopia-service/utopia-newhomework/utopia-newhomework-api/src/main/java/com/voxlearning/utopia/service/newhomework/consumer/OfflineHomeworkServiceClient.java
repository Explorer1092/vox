package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.service.OfflineHomeworkService;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/9/8
 */
public class OfflineHomeworkServiceClient implements OfflineHomeworkService {

    @ImportService(interfaceClass = OfflineHomeworkService.class)
    private OfflineHomeworkService remoteReference;

    @Override
    public MapMessage loadIndexData(Teacher teacher, List<String> homeworkIds, List<Long> clazzGroupIds) {
        return remoteReference.loadIndexData(teacher, homeworkIds, clazzGroupIds);
    }

    @Override
    public MapMessage assignOfflineHomework(Teacher teacher, Map<String, Object> homeworkJson, HomeworkSourceType homeworkSourceType) {
        return remoteReference.assignOfflineHomework(teacher, homeworkJson, homeworkSourceType);
    }
}
