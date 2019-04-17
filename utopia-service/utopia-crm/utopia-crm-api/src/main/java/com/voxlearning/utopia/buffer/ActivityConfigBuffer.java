package com.voxlearning.utopia.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ActivityConfigBuffer extends NearBuffer<List<ActivityConfig>> {

    private static final Logger log = LoggerFactory.getLogger(ActivityConfigBuffer.class);

    @Override
    public int estimateSize() {
        return dump().getData().size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<ActivityConfig> data) {

    }

    public List<ActivityConfig> getCanParticipateActivity(StudentDetail studentDetail) {
        List<ActivityConfig> result = new ArrayList<>();
        if (studentDetail == null || studentDetail.getClazz() == null) {
            return result;
        }

        Date now = new Date();

        try {
            List<ActivityConfig> data = super.dump().getData().stream()
                    .filter(i -> i.isStarting(now))
                    .collect(Collectors.toList());

            Long clazzId = studentDetail.getClazzId();
            Long schoolId = studentDetail.getClazz().getSchoolId();
            Integer clazzLevel = studentDetail.getClazzLevelAsInteger();
            Long regionCode = SafeConverter.toLong(studentDetail.getStudentSchoolRegionCode());

            for (ActivityConfig config : data) {
                if (CollectionUtils.isNotEmpty(config.getAreaIds())) {
                    if (!config.getAreaIds().contains(regionCode)) continue;
                } else {
                    if (!config.getSchoolIds().contains(schoolId)) continue;
                }
                if (CollectionUtils.isNotEmpty(config.getClazzIds())) {
                    if (!config.getClazzIds().contains(clazzId)) continue;
                }
                if (CollectionUtils.isNotEmpty(config.getClazzLevels())) {
                    if (!config.getClazzLevels().contains(clazzLevel)) continue;
                }
                result.add(config);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public List<ActivityConfig> loadBySchoolIdAreaIdClazzIds(Long schoolId, Integer areaId, List<Clazz> clazzList) {
        long areaLong = SafeConverter.toLong(areaId);

        List<ActivityConfig> result = new ArrayList<>();
        if (schoolId == null || areaId == null || CollectionUtils.isEmpty(clazzList)) {
            return result;
        }

        Set<Long> clazzIdList = clazzList.stream().map(Clazz::getId).collect(Collectors.toSet());
        Set<Integer> clazzLevelList = clazzList.stream().map(i -> i.getClazzLevel().getLevel()).collect(Collectors.toSet());

        for (ActivityConfig config : super.dump().getData()) {
            if (CollectionUtils.isNotEmpty(config.getAreaIds())) {
                if (!config.getAreaIds().contains(areaLong)) continue;
            } else {
                if (!config.getSchoolIds().contains(schoolId)) continue;
            }
            if (CollectionUtils.isNotEmpty(config.getClazzIds())) {
                if (!CollectionUtils.containsAny(config.getClazzIds(), clazzIdList)) continue;
            }
            if (CollectionUtils.isNotEmpty(config.getClazzLevels())) {
                if (!CollectionUtils.containsAny(config.getClazzLevels(), clazzLevelList)) continue;
            }
            result.add(config);
        }
        return result;
    }

}
