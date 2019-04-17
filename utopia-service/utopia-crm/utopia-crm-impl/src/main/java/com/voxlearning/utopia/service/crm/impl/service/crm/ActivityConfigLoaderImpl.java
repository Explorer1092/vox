package com.voxlearning.utopia.service.crm.impl.service.crm;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.service.crm.api.ActivityConfigLoader;
import com.voxlearning.utopia.service.crm.impl.dao.crm.ActivityConfigDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chongfeng.qi
 * @date 20181126
 * 趣味活动配置查询相关
 */
@Named
@ExposeService(interfaceClass = ActivityConfigLoader.class)
@Slf4j
public class ActivityConfigLoaderImpl implements ActivityConfigLoader {

    @Inject
    private ActivityConfigDao activityConfigDao;

    @Override
    public Map<Long, List<ActivityConfig>> loadClassesActivity(Collection<Long> clazzIds) {
        return activityConfigDao.loadClassesActivity(clazzIds);
    }

    @Override
    public List<ActivityConfig> loadTeacherActivity(Long teacherId, Date startTime, Date endTime) {
        DayRange dayRange = new DayRange(startTime.getTime(), endTime.getTime());

        List<ActivityConfig> activityConfigs = activityConfigDao.loadByApplicant(Collections.singletonList(teacherId), ActivityConfig.ROLE_TEACHER)
                .getOrDefault(teacherId, Collections.emptyList()).stream()
                .filter(i -> dayRange.contains(i.getCreateTime()))
                .collect(Collectors.toList());

        return activityConfigs;
    }

    @Override
    public List<ActivityConfig> loadTeacherActivity(Long teacherId, String startTime, String endTime) {
        try {
            Date startDate = DateUtils.parseDate(startTime + " 00:00:00", "yyyyMMdd HH:mm:ss");
            Date endDate = DateUtils.parseDate(endTime + " 23:59:59", "yyyyMMdd HH:mm:ss");
            return loadTeacherActivity(teacherId, startDate, endDate);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

}
