package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author guoqiang.li
 * @since 2016/9/8
 */
@ServiceVersion(version = "20160908")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface OfflineHomeworkService extends IPingable {

    MapMessage loadIndexData(Teacher teacher, List<String> homeworkIds, List<Long> clazzGroupIds);

    MapMessage assignOfflineHomework(Teacher teacher, Map<String, Object> homeworkJson, HomeworkSourceType homeworkSourceType);
}
