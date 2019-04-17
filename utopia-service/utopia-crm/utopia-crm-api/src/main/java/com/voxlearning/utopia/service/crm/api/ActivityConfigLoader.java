package com.voxlearning.utopia.service.crm.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.entity.crm.ActivityConfig;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chongfeng.qi
 * @date 20181126
 * 趣味活动配置查询相关
 */

@ServiceVersion(version = "20181206")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface ActivityConfigLoader {

    /**
     * 查询班级的老师布置的活动
     *
     * @param clazzIds
     * @return
     */
    Map<Long, List<ActivityConfig>> loadClassesActivity(Collection<Long> clazzIds);

    List<ActivityConfig> loadTeacherActivity(Long teacherId, Date startTime, Date endTime);

    List<ActivityConfig> loadTeacherActivity(Long teacherId, String startTime, String endTime);
}
